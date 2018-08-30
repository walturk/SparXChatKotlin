package proximedia.com.au.sparxkotlin.utils

/**
 * Created by pc on 19/10/2017.
 */

object PositionUtils {
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val p = 0.017453292519943295      // Math.PI / 180
        val earthRad2 = 12742000.0         // 2 * R; R = 6371 km
        val lat1p = lat1 * p
        val lat2p = lat2 * p
        val a = 0.5 - Math.cos(lat2p - lat1p) / 2 + Math.cos(lat1p) * Math.cos(lat2p) *
                (1 - Math.cos((lon2 - lon1) * p)) / 2
        return (earthRad2 * Math.asin(Math.sqrt(a))).toFloat()
    }
}