package es.vermutroute.app

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Finds each bar's coordinates from its address with the phone's own geocoder
 * (Google's, on most Android phones) and caches them. The test route is meant
 * to show if a result is off: each bar has a "Ver en Maps" link to check it.
 */
object Geocoding {
    private const val PREFS = "coords"

    fun loadCache(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        BARS.forEach { bar ->
            prefs.getString(bar.id, null)?.split(",")?.let { parts ->
                val lat = parts.getOrNull(0)?.toDoubleOrNull()
                val lng = parts.getOrNull(1)?.toDoubleOrNull()
                if (lat != null && lng != null) AppState.coords[bar.id] = LatLng(lat, lng)
            }
        }
    }

    suspend fun locateMissing(ctx: Context) {
        val missing = BARS.filter { it.id !in AppState.coords }
        if (missing.isEmpty()) {
            AppState.geocodeMessage = ""
            return
        }
        if (!Geocoder.isPresent()) {
            AppState.geocodeMessage = "Este móvil no puede buscar direcciones: no se pueden ubicar los bares."
            return
        }
        val geocoder = Geocoder(ctx, Locale("es", "ES"))
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var done = BARS.size - missing.size
        for (bar in missing) {
            AppState.geocodeMessage = "Ubicando los bares… $done/${BARS.size}"
            val found = withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(bar.query, 1)?.firstOrNull()
                } catch (e: Exception) {
                    null
                }
            }
            if (found != null) {
                val ll = LatLng(found.latitude, found.longitude)
                AppState.coords[bar.id] = ll
                prefs.edit().putString(bar.id, "${ll.lat},${ll.lng}").apply()
                done++
            }
        }
        val failed = BARS.count { it.id !in AppState.coords }
        AppState.geocodeMessage =
            if (failed == 0) "" else "No he podido ubicar $failed bares. Comprueba que tienes conexión y vuelve a abrir la app."
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        AppState.coords.clear()
    }
}
