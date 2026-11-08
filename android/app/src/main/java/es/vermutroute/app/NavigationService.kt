package es.vermutroute.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Foreground service with location: keeps guiding with vibrations while the
 * phone is locked in a pocket. Lives in the same process as the UI and
 * reads/writes [AppState].
 */
class NavigationService : Service(), LocationListener {

    companion object {
        private const val CHANNEL_NAV = "nav"
        private const val CHANNEL_ARRIVAL = "arrival"
        private const val NOTIF_NAV = 1
        private const val NOTIF_ARRIVAL = 2

        const val ARRIVE_M = 25.0
        const val NEAR_M = 100.0
        private const val TICK_MS = 500L

        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, NavigationService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, NavigationService::class.java))
        }

        /** Called from the UI when the user moves on to the next stop. */
        fun resetGuidance() {
            instance?.resetForNewStop()
        }

        fun repeatLast(ctx: Context) {
            AppState.nav.lastPattern?.let { Haptics.play(ctx, it) }
        }

        fun manualArrival() {
            instance?.arrive()
        }

        private var instance: NavigationService? = null
    }

    private lateinit var lm: LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null

    private var last: Location? = null
    private var headingDeg: Double? = null
    private var headingFrom: Location? = null

    // Distance history for the radar ("te alejas").
    private val history = ArrayDeque<Pair<Long, Double>>()
    private var lastSignalAt = 0L
    private var offCourse = false
    private var lastTurn: String? = null
    private var lastTurnAt = 0L

    private val tick = object : Runnable {
        override fun run() {
            evaluate()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        lm = getSystemService(LocationManager::class.java)
        createChannels()
        ServiceCompat.startForeground(
            this, NOTIF_NAV, navNotification("Guiándote con vibraciones"),
            if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0,
        )
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VermutRoute:nav")
            .apply { acquire(4 * 60 * 60 * 1000L) }
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this, Looper.getMainLooper())
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 0f, this, Looper.getMainLooper())
            }
        } catch (e: SecurityException) {
            AppState.nav = NavStatus("Sin permiso de ubicación", "Activa el permiso de ubicación precisa en los ajustes de la app.")
        } catch (e: IllegalArgumentException) {
            AppState.nav = NavStatus("GPS no disponible", "Este móvil no tiene GPS o está desactivado.")
        }
        AppState.navigating = true
        resetForNewStop()
        handler.post(tick)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        try {
            lm.removeUpdates(this)
        } catch (_: SecurityException) {
        }
        wakeLock?.let { if (it.isHeld) it.release() }
        Haptics.cancel(this)
        AppState.navigating = false
        instance = null
        super.onDestroy()
    }

    // ---- location ----

    override fun onLocationChanged(location: Location) {
        val prev = last
        // Prefer GPS; ignore a coarse network fix once GPS is giving us better ones.
        if (prev != null && location.provider == LocationManager.NETWORK_PROVIDER &&
            prev.provider == LocationManager.GPS_PROVIDER && location.accuracy > prev.accuracy * 2
        ) return
        last = location

        // Heading = the direction you are walking, from GPS. A phone in a pocket
        // cannot use its compass reliably, so we need you to be moving.
        if (location.hasBearing() && location.hasSpeed() && location.speed > 0.6f) {
            headingDeg = location.bearing.toDouble()
            headingFrom = location
        } else {
            val from = headingFrom
            if (from == null) {
                headingFrom = location
            } else if (from.distanceTo(location) > 8f) {
                headingDeg = from.bearingTo(location).toDouble().let { (it + 360.0) % 360.0 }
                headingFrom = location
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    // ---- guidance ----

    private fun resetForNewStop() {
        history.clear()
        lastSignalAt = 0L
        offCourse = false
        lastTurn = null
        lastTurnAt = 0L
        AppState.arrived = false
        val stop = AppState.currentStop
        updateNavNotification(
            if (stop == null) "Ruta terminada"
            else if (AppState.surprise) "Hacia la parada ${AppState.stopIndex + 1}"
            else "Hacia ${stop.name}",
        )
    }

    private fun signal(pattern: LongArray, now: Long) {
        Haptics.play(this, pattern)
        lastSignalAt = now
        AppState.nav = AppState.nav.copy(lastPattern = pattern)
    }

    private fun status(headline: String, detail: String, d: Double?, arrow: Double?, relative: Boolean) {
        AppState.nav = AppState.nav.copy(
            headline = headline, detail = detail, distanceM = d, arrowDeg = arrow, arrowRelative = relative,
        )
    }

    private fun evaluate() {
        val stop = AppState.currentStop ?: return
        if (AppState.arrived) return
        val loc = last ?: run {
            status("Buscando GPS…", "Sal a la calle para que el móvil encuentre la señal.", null, null, false)
            return
        }
        val here = LatLng(loc.latitude, loc.longitude)
        val d = Geo.distance(here, stop.pos)
        val target = Geo.bearing(here, stop.pos)
        val now = SystemClock.elapsedRealtime()

        if (d <= ARRIVE_M) {
            arrive()
            return
        }

        history.addLast(now to d)
        while (history.isNotEmpty() && now - history.first().first > 40_000) history.removeFirst()

        val heading = headingDeg
        val err = heading?.let { Geo.angleDiff(target, it) }
        val arrow = err ?: target
        val relative = err != null

        when (AppState.lang) {
            Lang.A -> {
                if (err == null) {
                    status("Echa a andar", "Necesito que camines unos metros para saber hacia dónde vas.", d, arrow, relative)
                    return
                }
                val near = d < NEAR_M
                if (abs(err) <= 40.0) {
                    if (offCourse) {
                        offCourse = false
                        signal(Patterns.A_BACK, now)
                    } else if (now - lastSignalAt >= if (near) 1000 else 3000) {
                        signal(if (near) Patterns.A_NEAR else Patterns.A_OK, now)
                    }
                    status(
                        if (near) "Muy caliente" else "Caliente",
                        if (near) "Estás a menos de 100 m. Mira las fachadas." else "Sigue en esta dirección.",
                        d, arrow, relative,
                    )
                } else {
                    offCourse = true
                    status("Frío", "Silencio: gira despacio hasta notar otra vez el tic-tic.", d, arrow, relative)
                }
            }

            Lang.B -> {
                if (err == null) {
                    status("Echa a andar", "Necesito que camines unos metros para saber hacia dónde vas.", d, arrow, relative)
                    return
                }
                val turn = when {
                    abs(err) <= 45.0 -> "recto"
                    abs(err) > 135.0 -> "vuelta"
                    err > 0 -> "derecha"
                    else -> "izquierda"
                }
                val pattern = when (turn) {
                    "izquierda" -> Patterns.B_LEFT
                    "derecha" -> Patterns.B_RIGHT
                    "vuelta" -> Patterns.B_UTURN
                    else -> null
                }
                if (pattern != null && (turn != lastTurn || now - lastTurnAt >= 8000)) {
                    signal(pattern, now)
                    lastTurnAt = now
                }
                lastTurn = turn
                val (h, det) = when (turn) {
                    "izquierda" -> "Gira a la izquierda" to "Dos toques cortos: en el próximo cruce, a la izquierda."
                    "derecha" -> "Gira a la derecha" to "Un toque largo: en el próximo cruce, a la derecha."
                    "vuelta" -> "Media vuelta" to "Ráfaga de toques: vas en dirección contraria."
                    else -> "Recto" to "Silencio: sigue por esta calle."
                }
                status(h, det, d, arrow, relative)
            }

            Lang.C -> {
                val minRecent = history.filter { now - it.first <= 30_000 }.minOf { it.second }
                val away = d - minRecent > 12.0
                val interval = when {
                    away -> 3000L
                    d > 300 -> 5000L
                    d > NEAR_M -> 2000L
                    else -> 700L
                }
                if (now - lastSignalAt >= interval) {
                    signal(if (away) Patterns.C_AWAY else Patterns.C_BEAT, now)
                }
                val (h, det) = when {
                    away -> "Te alejas" to "Zumbido largo: prueba otra calle."
                    d > 300 -> "Lejos" to "Un latido cada 5 s."
                    d > NEAR_M -> "Te acercas" to "Un latido cada 2 s."
                    else -> "Casi estás" to "Latido casi continuo: el bar está muy cerca."
                }
                status(h, det, d, arrow, relative)
            }
        }
    }

    fun arrive() {
        if (AppState.arrived) return
        AppState.arrived = true
        Haptics.play(this, Patterns.ARRIVE)
        AppState.nav = AppState.nav.copy(headline = "¡Has llegado!", detail = "", lastPattern = Patterns.ARRIVE)
        AppState.screen = Screen.ARRIVAL
        val stop = AppState.currentStop
        val text = if (stop != null) "Levanta la vista: estás en ${stop.name}" else "Has llegado"
        getSystemService(NotificationManager::class.java).notify(
            NOTIF_ARRIVAL,
            NotificationCompat.Builder(this, CHANNEL_ARRIVAL)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("¡Has llegado!")
                .setContentText(text)
                .setContentIntent(openApp())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build(),
        )
        updateNavNotification("Has llegado · abre la app para el fun fact")
    }

    // ---- notifications ----

    private fun createChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_NAV, "Navegación", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Aviso fijo mientras la app te guía con vibraciones"
                enableVibration(false)
                setSound(null, null)
            },
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ARRIVAL, "Llegada", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Cuando llegas a una vermutería"
                enableVibration(false) // the app plays its own arrival pattern
            },
        )
    }

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        this, 0,
        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun navNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_NAV)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Ruta del Vermut")
            .setContentText(text)
            .setContentIntent(openApp())
            .setOngoing(true)
            .setSilent(true)
            .build()

    private fun updateNavNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIF_NAV, navNotification(text))
    }
}

fun formatDistance(m: Double): String =
    if (m >= 1000) String.format(java.util.Locale("es", "ES"), "%.1f km", m / 1000) else "${(m / 10).roundToInt() * 10} m"
