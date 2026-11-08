package es.vermutroute.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Screen { HOME, PICK, ROUTE, NAVIGATE, ARRIVAL, RATING, SUMMARY, HAPTIC_TEST }

data class Stop(val id: String, val name: String, val address: String, val fact: String, val pos: LatLng)

/** What the navigation service is doing right now, for the pocket screen. */
data class NavStatus(
    val headline: String = "Buscando GPS…",
    val detail: String = "Sal a la calle para que el móvil encuentre la señal.",
    val distanceM: Double? = null,
    /** Target bearing relative to where you walk (if known), else to the north. */
    val arrowDeg: Double? = null,
    val arrowRelative: Boolean = false,
    val lastPattern: LongArray? = null,
)

data class Rating(
    val stopName: String,
    val type: String,
    val scores: Map<String, Int>,
    val again: String,
    val notes: String,
) {
    val average: Double get() = if (scores.isEmpty()) 0.0 else scores.values.average()
}

/** Single-process app state shared by the UI and the navigation service. */
object AppState {
    var screen by mutableStateOf(Screen.HOME)

    // Bar coordinates found by the geocoder, by bar id.
    val coords = mutableStateMapOf<String, LatLng>()
    var geocodeMessage by mutableStateOf("")

    // Planning
    val selected = mutableStateListOf<String>()
    var surprise by mutableStateOf(false)
    var lang by mutableStateOf(Lang.A)
    var planDayOffset by mutableStateOf(0)
    var planHour by mutableStateOf(13)

    // Route in progress
    var route by mutableStateOf(listOf<Stop>())
    var stopIndex by mutableStateOf(0)
    var nav by mutableStateOf(NavStatus())
    var arrived by mutableStateOf(false)
    var navigating by mutableStateOf(false)
    val ratings = mutableStateListOf<Rating>()

    val currentStop: Stop? get() = route.getOrNull(stopIndex)

    fun resetRoute() {
        route = emptyList()
        stopIndex = 0
        nav = NavStatus()
        arrived = false
        navigating = false
        ratings.clear()
    }
}
