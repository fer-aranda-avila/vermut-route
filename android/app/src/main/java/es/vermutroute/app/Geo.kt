package es.vermutroute.app

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLng(val lat: Double, val lng: Double)

object Geo {
    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Great-circle distance in metres. */
    fun distance(a: LatLng, b: LatLng): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * EARTH_RADIUS_M * atan2(sqrt(h), sqrt(1 - h))
    }

    /** Initial bearing from a to b, in degrees clockwise from north (0..360). */
    fun bearing(a: LatLng, b: LatLng): Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /** Signed difference target − heading in (−180, 180]. Positive = target is to the right. */
    fun angleDiff(target: Double, heading: Double): Double {
        var d = (target - heading) % 360.0
        if (d > 180.0) d -= 360.0
        if (d <= -180.0) d += 360.0
        return d
    }

    /** The point `metres` away from `from` in direction `bearingDeg`. */
    fun offset(from: LatLng, metres: Double, bearingDeg: Double): LatLng {
        val dr = metres / EARTH_RADIUS_M
        val br = Math.toRadians(bearingDeg)
        val lat1 = Math.toRadians(from.lat)
        val lng1 = Math.toRadians(from.lng)
        val lat2 = kotlin.math.asin(sin(lat1) * cos(dr) + cos(lat1) * sin(dr) * cos(br))
        val lng2 = lng1 + atan2(sin(br) * sin(dr) * cos(lat1), cos(dr) - sin(lat1) * sin(lat2))
        return LatLng(Math.toDegrees(lat2), Math.toDegrees(lng2))
    }

    fun compass(bearingDeg: Double): String {
        val names = listOf("norte", "noreste", "este", "sureste", "sur", "suroeste", "oeste", "noroeste")
        return names[(((bearingDeg + 22.5) % 360.0) / 45.0).toInt()]
    }
}
