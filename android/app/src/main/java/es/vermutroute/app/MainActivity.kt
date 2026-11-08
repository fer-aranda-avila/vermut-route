package es.vermutroute.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Calendar

private val Bg = Color(0xFF15100E)
private val Surface1 = Color(0xFF1C1513)
private val Surface2 = Color(0xFF2E2320)
private val Line = Color(0xFF3D302C)
private val Ink = Color(0xFFF4ECE2)
private val Muted = Color(0xFFB5A496)
private val Accent = Color(0xFFE5503F)
private val Ok = Color(0xFFB4C25E)
private val Warn = Color(0xFFE5A93F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Geocoding.loadCache(this)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Accent, onPrimary = Bg, background = Bg, onBackground = Ink,
                    surface = Surface1, onSurface = Ink, secondary = Ok,
                ),
            ) {
                Box(Modifier.fillMaxSize().background(Bg).safeDrawingPadding()) { App() }
            }
        }
    }
}

// ---------------------------------------------------------------- shell

@Composable
private fun App() {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { Geocoding.locateMissing(ctx) }

    BackHandler(enabled = AppState.screen != Screen.HOME && AppState.screen != Screen.NAVIGATE) {
        AppState.screen = when (AppState.screen) {
            Screen.ROUTE -> Screen.PICK
            Screen.RATING -> Screen.ARRIVAL
            else -> Screen.HOME
        }
    }

    when (AppState.screen) {
        Screen.HOME -> HomeScreen()
        Screen.PICK -> PickScreen()
        Screen.ROUTE -> RouteScreen()
        Screen.NAVIGATE -> NavigateScreen()
        Screen.ARRIVAL -> ArrivalScreen()
        Screen.RATING -> RatingScreen()
        Screen.SUMMARY -> SummaryScreen()
        Screen.HAPTIC_TEST -> HapticTestScreen()
    }
}

@Composable
private fun Page(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) { content() }
}

@Composable
private fun Eyebrow(text: String, color: Color = Muted) =
    Text(text.uppercase(), color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp, letterSpacing = 1.5.sp)

@Composable
private fun Title(text: String, size: Int = 34) =
    Text(text.uppercase(), color = Ink, fontWeight = FontWeight.Black, fontSize = size.sp, lineHeight = (size * 1.0).sp)

@Composable
private fun Body(text: String, color: Color = Muted, size: Int = 15) =
    Text(text, color = color, fontSize = size.sp, lineHeight = (size * 1.4).sp)

@Composable
private fun Primary(text: String, enabled: Boolean = true, onClick: () -> Unit) = Button(
    onClick = onClick, enabled = enabled,
    modifier = Modifier.fillMaxWidth().height(56.dp),
    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
) { Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp) }

@Composable
private fun Secondary(text: String, onClick: () -> Unit) = OutlinedButton(
    onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
) { Text(text, color = Ink, fontSize = 15.sp) }

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(18.dp))
            .background(if (selected) Ink else Color.Transparent)
            .border(1.dp, if (selected) Ink else Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 40.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = if (selected) Bg else Ink, fontSize = 14.sp) }
}

@Composable
private fun Card(selected: Boolean = false, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    var m = Modifier.fillMaxWidth().clip(shape)
        .background(if (selected) Surface2 else Surface1)
        .border(1.dp, if (selected) Accent else Surface2, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { content() }
}

private fun hasLocationPermission(ctx: Context) =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun lastKnown(ctx: Context): LatLng? {
    if (!hasLocationPermission(ctx)) return null
    val lm = ctx.getSystemService(LocationManager::class.java)
    val loc = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time } ?: return null
    return LatLng(loc.latitude, loc.longitude)
}

private val PERMISSIONS: Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
}.toTypedArray()

/** Nearest-neighbour order from where you are (or the chosen order if unknown). */
private fun orderStops(start: LatLng?, stops: List<Stop>): List<Stop> {
    if (start == null) return stops
    val left = stops.toMutableList()
    val out = mutableListOf<Stop>()
    var here: LatLng = start
    while (left.isNotEmpty()) {
        val next = left.minBy { Geo.distance(here, it.pos) }
        out += next
        left -= next
        here = next.pos
    }
    return out
}

// ---------------------------------------------------------------- home

@Composable
private fun HomeScreen() {
    val ctx = LocalContext.current
    var testMessage by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            val here = lastKnown(ctx)
            if (here == null) {
                testMessage = "Todavía no sé dónde estás. Sal a la calle, espera unos segundos y vuelve a pulsar."
            } else {
                startTestRoute(ctx, here)
            }
        } else {
            testMessage = "Sin permiso de ubicación precisa no puedo guiarte."
        }
    }

    Page {
        Spacer(Modifier.height(12.dp))
        Eyebrow("Ruta del Vermut · Madrid")
        Title("Sin mapa.\nSolo vibraciones.", 46)
        Body("Elige tus vermuterías, guarda el móvil en el bolsillo y deja que el pulso te lleve de barra en barra.", size = 17)
        if (AppState.geocodeMessage.isNotEmpty()) Body(AppState.geocodeMessage, Warn, 14)
        Spacer(Modifier.height(8.dp))
        Primary("Empezar ruta") { AppState.screen = Screen.PICK }
        Secondary("Probar las vibraciones") { AppState.screen = Screen.HAPTIC_TEST }
        Secondary("Prueba rápida: un destino a 200 m de aquí") {
            if (hasLocationPermission(ctx)) {
                val here = lastKnown(ctx)
                if (here == null) testMessage = "Todavía no sé dónde estás. Sal a la calle, espera unos segundos y vuelve a pulsar."
                else startTestRoute(ctx, here)
            } else {
                launcher.launch(PERMISSIONS)
            }
        }
        if (testMessage.isNotEmpty()) Body(testMessage, Warn, 14)
        Spacer(Modifier.height(8.dp))
        Eyebrow("Antes de salir")
        Body("Para que siga vibrando con la pantalla apagada, quita la app de la optimización de batería. Es importante en Xiaomi, Samsung y Huawei.", size = 14)
        TextButton(onClick = {
            ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }) { Text("Abrir ajustes de batería", color = Accent) }
        TextButton(onClick = {
            Geocoding.clear(ctx)
            AppState.geocodeMessage = "Volviendo a ubicar los bares… reinicia la app."
        }) { Text("Volver a ubicar los bares", color = Muted) }
    }
}

private fun startTestRoute(ctx: Context, here: LatLng) {
    AppState.resetRoute()
    AppState.route = listOf(
        Stop(
            "test", "Destino de prueba", "200 m al norte de donde estabas",
            "Esto es una parada de prueba: sirve para comprobar las vibraciones sin ir al centro.",
            Geo.offset(here, 200.0, 0.0),
        ),
    )
    AppState.screen = Screen.ROUTE
}

// ---------------------------------------------------------------- pick

@Composable
private fun PickScreen() {
    val ctx = LocalContext.current
    var message by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("Todos") }

    val today = Calendar.getInstance()
    val todayWd = (today.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
    val planWd = (todayWd + AppState.planDayOffset) % 7

    fun buildRoute() {
        val stops = BARS.filter { it.id in AppState.selected }.mapNotNull { bar ->
            AppState.coords[bar.id]?.let { Stop(bar.id, bar.name, bar.address, bar.fact, it) }
        }
        AppState.resetRoute()
        AppState.route = orderStops(lastKnown(ctx), stops)
        AppState.screen = Screen.ROUTE
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) buildRoute()
        else message = "Sin permiso de ubicación precisa no puedo guiarte."
    }

    Page {
        TextButton(onClick = { AppState.screen = Screen.HOME }) { Text("‹ Volver", color = Ink) }
        Title("¿Dónde te apetece brindar?", 30)
        Body("Marca entre 3 y 5 barras. La app las ordena para andar lo mínimo.", size = 14)

        Eyebrow("¿Cuándo vas?")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0..6) {
                val wd = (todayWd + i) % 7
                val label = when (i) {
                    0 -> "Hoy"
                    1 -> "Mañana"
                    else -> DAY_NAMES[wd].replaceFirstChar { it.uppercase() }
                }
                Chip(label, AppState.planDayOffset == i) { AppState.planDayOffset = i }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (h in 11..23) Chip("$h:00", AppState.planHour == h) { AppState.planHour = h }
        }

        val zones = listOf("Todos") + BARS.map { it.zone }.distinct()
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            zones.forEach { z -> Chip(z, zone == z) { zone = z } }
        }

        var closedPicked = 0
        BARS.forEach { bar ->
            val sel = bar.id in AppState.selected
            val st = bar.statusAt(planWd, AppState.planHour.toDouble())
            if (sel && st == OpenStatus.CLOSED) closedPicked++
            if (zone != "Todos" && bar.zone != zone) return@forEach
            val pos = AppState.coords[bar.id]
            Card(selected = sel, onClick = {
                if (sel) AppState.selected.remove(bar.id) else AppState.selected.add(bar.id)
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(bar.name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("${bar.zone} · ${bar.address}", color = Muted, fontSize = 13.sp)
                        when {
                            st == OpenStatus.CLOSED -> Text(
                                "Puede estar cerrado el ${DAY_NAMES[planWd]} a las ${AppState.planHour}:00 · revisa el horario",
                                color = Warn, fontSize = 12.sp,
                            )
                            st == OpenStatus.UNKNOWN -> Text("Horario sin confirmar", color = Muted, fontSize = 12.sp)
                            bar.note != null -> Text(bar.note, color = Muted, fontSize = 12.sp)
                        }
                        if (pos == null) Text("Sin ubicar todavía", color = Warn, fontSize = 12.sp)
                    }
                    Box(
                        Modifier.size(26.dp).clip(CircleShape)
                            .background(if (sel) Accent else Color.Transparent)
                            .border(1.5.dp, if (sel) Accent else Line, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { if (sel) Text("✓", color = Bg, fontWeight = FontWeight.Bold) }
                }
                if (pos != null) {
                    Text(
                        "Ver en Maps",
                        color = Accent, fontSize = 13.sp,
                        modifier = Modifier.clickable {
                            val uri = Uri.parse("geo:${pos.lat},${pos.lng}?q=${pos.lat},${pos.lng}(${Uri.encode(bar.name)})")
                            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        }.padding(vertical = 6.dp),
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface1).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Modo sorpresa", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("No verás el orden ni el bar hasta llegar a cada uno", color = Muted, fontSize = 12.sp)
            }
            Switch(
                checked = AppState.surprise, onCheckedChange = { AppState.surprise = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Accent),
            )
        }

        val count = AppState.selected.size
        val located = AppState.selected.count { it in AppState.coords }
        val (hint, hintColor) = when {
            count == 0 -> "Elige al menos un bar" to Warn
            located < count -> "${count - located} bares aún sin ubicar: no entrarán en la ruta" to Warn
            closedPicked > 0 -> (if (closedPicked == 1) "1 bar puede estar cerrado a esa hora" else "$closedPicked bares pueden estar cerrados a esa hora") to Warn
            count in 3..5 -> "Buen plan para un aperitivo" to Ok
            else -> "Recomendado: de 3 a 5 bares" to Muted
        }
        Text("$count paradas", color = Ink, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(hint, color = hintColor, fontSize = 13.sp)
        if (message.isNotEmpty()) Body(message, Warn, 14)
        Primary("Crear ruta", enabled = located > 0) {
            if (hasLocationPermission(ctx)) buildRoute() else launcher.launch(PERMISSIONS)
        }
    }
}

// ---------------------------------------------------------------- route

@Composable
private fun RouteScreen() {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            NavigationService.start(ctx)
            AppState.screen = Screen.NAVIGATE
        }
    }
    val surprise = AppState.surprise && AppState.route.none { it.id == "test" }

    Page {
        TextButton(onClick = { AppState.screen = Screen.PICK }) { Text("‹ Volver", color = Ink) }
        Title(if (surprise) "Tu ruta sorpresa" else "Tu ruta", 34)
        if (surprise) Body("Conoces los bares que elegiste, pero no el orden: cada uno se revela al llegar.", Ink, 14)
        AppState.route.forEachIndexed { i, s ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                    Text(if (surprise) "?" else "${i + 1}", color = Bg, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text(if (surprise) "Parada ${i + 1}" else s.name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(if (surprise) "Se revela al llegar" else s.address, color = Muted, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Eyebrow("¿Cómo quieres sentir el camino?")
        Lang.entries.forEach { l ->
            Card(selected = AppState.lang == l, onClick = { AppState.lang = l }) {
                Text("${l.key} · ${l.title}", color = Ink, fontWeight = FontWeight.SemiBold)
                Text(l.desc, color = Muted, fontSize = 13.sp)
            }
        }
        Body("Lleva el móvil en el bolsillo y camina: la dirección se calcula con el GPS mientras andas.", size = 13)
        Primary("Guardar el móvil y salir") {
            if (hasLocationPermission(ctx)) {
                NavigationService.start(ctx)
                AppState.screen = Screen.NAVIGATE
            } else {
                launcher.launch(PERMISSIONS)
            }
        }
    }
}

// ---------------------------------------------------------------- navigate

@Composable
private fun NavigateScreen() {
    val ctx = LocalContext.current
    var help by remember { mutableStateOf(false) }
    LaunchedEffect(help) {
        if (help) {
            delay(10_000)
            help = false
        }
    }
    val stop = AppState.currentStop
    val nav = AppState.nav
    val total = AppState.route.size

    Page {
        Eyebrow("Parada ${AppState.stopIndex + 1} de $total · ${AppState.lang.key} · ${AppState.lang.title}")
        Body("Hacia", size = 14)
        Title(if (AppState.surprise && stop?.id != "test") "Vermutería sorpresa" else stop?.name ?: "—", 28)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp)).background(Surface1),
            contentAlignment = Alignment.Center,
        ) {
            if (help && nav.arrowDeg != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("↑", color = Accent, fontSize = 96.sp, modifier = Modifier.rotate(nav.arrowDeg.toFloat()))
                    Text(
                        if (nav.arrowRelative) "respecto a hacia donde andas" else "respecto al norte (${Geo.compass(nav.arrowDeg)})",
                        color = Muted, fontSize = 12.sp,
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(nav.headline, color = Ink, fontWeight = FontWeight.Black, fontSize = 30.sp)
                    Text("Pantalla apagada: sigue vibrando", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Body(nav.detail, Ink, 16)
        if (help) {
            Body(
                nav.distanceM?.let { "Estás a ${formatDistance(it)} del bar. La flecha se esconde en 10 s." }
                    ?: "Aún no tengo señal de GPS.",
                Warn, 14,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { NavigationService.repeatLast(ctx) }, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Repetir", color = Ink)
            }
            OutlinedButton(onClick = { help = true }, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Me he perdido", color = Ink)
            }
        }
        Primary("He llegado") { NavigationService.manualArrival() }
        TextButton(onClick = {
            NavigationService.stop(ctx)
            AppState.screen = if (AppState.ratings.isEmpty()) Screen.HOME else Screen.SUMMARY
        }) { Text("Terminar la ruta", color = Muted) }
    }
}

// ---------------------------------------------------------------- arrival

@Composable
private fun ArrivalScreen() {
    val ctx = LocalContext.current
    val stop = AppState.currentStop
    Column(
        Modifier.fillMaxSize().background(Accent).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Eyebrow("Parada ${AppState.stopIndex + 1} de ${AppState.route.size} · ¡Llegada!", Bg)
        Text("Levanta la vista. Estás en", color = Bg, fontSize = 16.sp)
        Text((stop?.name ?: "").uppercase(), color = Bg, fontWeight = FontWeight.Black, fontSize = 46.sp, lineHeight = 46.sp)
        Text(stop?.address ?: "", color = Bg, fontSize = 15.sp)
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Bg).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Eyebrow("Fun fact", Accent)
            Text(stop?.fact ?: "", color = Ink, fontSize = 20.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold)
            if (stop?.id != "test") Text("Fuente: plano de vermuterías de la Academia Madrileña de Gastronomía", color = Muted, fontSize = 12.sp)
        }
        Button(
            onClick = { AppState.screen = Screen.RATING },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Bg, contentColor = Ink),
        ) { Text("Pide tu vermut y valóralo", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
        TextButton(onClick = { goToNextStop(ctx) }, modifier = Modifier.fillMaxWidth()) {
            Text("Saltar la ficha", color = Bg)
        }
    }
}

private fun goToNextStop(ctx: Context) {
    if (AppState.stopIndex + 1 < AppState.route.size) {
        AppState.stopIndex += 1
        AppState.nav = NavStatus()
        NavigationService.resetGuidance()
        AppState.screen = Screen.NAVIGATE
    } else {
        NavigationService.stop(ctx)
        AppState.screen = Screen.SUMMARY
    }
}

// ---------------------------------------------------------------- rating

private val CRITERIA = listOf(
    "aroma" to ("Aroma" to "1 plano · 5 complejo"),
    "balance" to ("Equilibrio dulce / amargo" to "1 empalaga · 5 redondo"),
    "body" to ("Cuerpo y textura" to "1 aguado · 5 sedoso"),
    "serve" to ("Servicio" to "temperatura, hielo, naranja"),
    "tapa" to ("La tapa que lo acompaña" to "1 nada · 5 memorable"),
    "price" to ("Calidad / precio" to "1 caro · 5 regalado"),
)

@Composable
private fun RatingScreen() {
    val ctx = LocalContext.current
    val stop = AppState.currentStop
    var type by remember { mutableStateOf("Rojo") }
    var again by remember { mutableStateOf("Sí") }
    var notes by remember { mutableStateOf("") }
    val scores = remember { mutableStateMapOf<String, Int>() }
    val avg = if (scores.isEmpty()) null else scores.values.average()

    Page {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Ficha de cata · ${AppState.stopIndex + 1}/${AppState.route.size}")
                Title(stop?.name ?: "", 26)
            }
            Text(avg?.let { String.format(java.util.Locale("es", "ES"), "%.1f", it) } ?: "–", color = Accent, fontWeight = FontWeight.Black, fontSize = 40.sp)
        }
        Body("¿Qué has pedido?", size = 14)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Rojo", "Blanco", "Rosado", "Reserva").forEach { t -> Chip(t, type == t) { type = t } }
        }
        CRITERIA.forEach { (id, labels) ->
            Row(verticalAlignment = Alignment.Bottom) {
                Text(labels.first, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(labels.second, color = Muted, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (n in 1..5) {
                    val on = scores[id] == n
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (on) Accent else Surface1)
                            .border(1.dp, if (on) Accent else Line, RoundedCornerShape(10.dp))
                            .clickable { scores[id] = n },
                        contentAlignment = Alignment.Center,
                    ) { Text("$n", color = if (on) Bg else Ink, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Text("¿Repetirías aquí?", color = Ink, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Sí", "Quizá", "No").forEach { a -> Chip(a, again == a) { again = a } }
        }
        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            label = { Text("Notas") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = 3,
        )
        val last = AppState.stopIndex + 1 >= AppState.route.size
        Primary(if (last) "Guardar y ver el ranking" else "Guardar y a por el siguiente") {
            if (stop != null) AppState.ratings += Rating(stop.name, type, scores.toMap(), again, notes)
            goToNextStop(ctx)
        }
        TextButton(onClick = {
            if (stop != null) AppState.ratings += Rating(stop.name, type, scores.toMap(), again, notes)
            NavigationService.stop(ctx)
            AppState.screen = Screen.SUMMARY
        }) { Text("Terminar la ruta aquí", color = Muted) }
    }
}

// ---------------------------------------------------------------- summary

@Composable
private fun SummaryScreen() {
    val ranking = AppState.ratings.sortedByDescending { it.average }
    Page {
        Eyebrow("Ruta completada")
        Title("Tu podio del vermut", 40)
        if (ranking.isEmpty()) Body("No has valorado ningún vermut esta vez.")
        ranking.forEachIndexed { i, r ->
            Card(selected = i == 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", color = if (i == 0) Accent else Muted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(24.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.stopName, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text("${r.type} · repetiría: ${r.again.lowercase()}", color = Muted, fontSize = 12.sp)
                    }
                    Text(
                        if (r.scores.isEmpty()) "–" else String.format(java.util.Locale("es", "ES"), "%.1f", r.average),
                        color = if (i == 0) Accent else Ink, fontWeight = FontWeight.Black, fontSize = 22.sp,
                    )
                }
                if (r.notes.isNotBlank()) Text(r.notes, color = Muted, fontSize = 13.sp)
            }
        }
        Primary("Otra ruta") {
            AppState.resetRoute()
            AppState.screen = Screen.HOME
        }
    }
}

// ---------------------------------------------------------------- haptic test

@Composable
private fun HapticTestScreen() {
    val ctx = LocalContext.current
    var lang by remember { mutableStateOf(Lang.A) }
    Page {
        TextButton(onClick = { AppState.screen = Screen.HOME }) { Text("‹ Volver", color = Ink) }
        Title("Probar las vibraciones", 30)
        Body("Pulsa una señal y guarda el móvil en el bolsillo para notarla. En la app las vibraciones van como «alarma», así que no dependen del ajuste de vibración al tocar.", size = 14)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Lang.entries.forEach { l -> Chip("${l.key} · ${l.title}", lang == l) { lang = l } }
        }
        Body(lang.desc, Ink, 14)
        SIGNALS[lang].orEmpty().forEach { s ->
            Card(onClick = { Haptics.preview(ctx, s) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.name, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text(s.desc, color = Muted, fontSize = 13.sp)
                    }
                    Box(Modifier.size(44.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                        Text("▶", color = Bg)
                    }
                }
            }
        }
        Secondary("Parar") { Haptics.cancel(ctx) }
    }
}
