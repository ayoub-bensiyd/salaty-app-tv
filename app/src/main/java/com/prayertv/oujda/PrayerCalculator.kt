package com.prayertv.oujda

import kotlin.math.*

/**
 * حساب أوقات الصلاة باستخدام طريقة رابطة العالم الإسلامي
 * Oujda, Morocco: Latitude 34.68, Longitude -1.91, GMT+1
 */
class PrayerCalculator {
    
    companion object {
        private const val LATITUDE = 34.68
        private const val LONGITUDE = -1.91
        private const val TIMEZONE = 1.0 // GMT+1
        
        // Muslim World League parameters
        private const val FAJR_ANGLE = 18.0
        private const val ISHA_ANGLE = 17.0
        
        data class PrayerTimes(
            val fajr: String,
            val dhuhr: String,
            val asr: String,
            val maghrib: String,
            val isha: String
        )
        
        fun calculatePrayerTimes(
            year: Int,
            month: Int,
            day: Int,
            adjustments: Map<String, Int> = emptyMap()
        ): PrayerTimes {
            val jd = julianDate(year, month, day)
            
            // Calculate equation of time and declination
            val eqt = equationOfTime(jd)
            val decl = sunDeclination(jd)
            
            // Calculate prayer times
            val fajr = calculateTime(FAJR_ANGLE, eqt, decl, adjustments["fajr"] ?: 0)
            val dhuhr = calculateDhuhr(eqt, adjustments["dhuhr"] ?: 0)
            val asr = calculateAsr(eqt, decl, adjustments["asr"] ?: 0)
            val maghrib = calculateMaghrib(eqt, decl, adjustments["maghrib"] ?: 0)
            val isha = calculateTime(ISHA_ANGLE, eqt, decl, adjustments["isha"] ?: 0, isFajr = false)
            
            return PrayerTimes(fajr, dhuhr, asr, maghrib, isha)
        }
        
        private fun julianDate(year: Int, month: Int, day: Int): Double {
            var y = year
            var m = month
            if (m <= 2) {
                y -= 1
                m += 12
            }
            val a = floor(y / 100.0)
            val b = 2 - a + floor(a / 4.0)
            return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
        }
        
        private fun equationOfTime(jd: Double): Double {
            val d = jd - 2451545.0
            val g = 357.529 + 0.98560028 * d
            val q = 280.459 + 0.98564736 * d
            val l = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))
            val e = 23.439 - 0.00000036 * d
            val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))
            return (q - ra) / 15.0
        }
        
        private fun sunDeclination(jd: Double): Double {
            val d = jd - 2451545.0
            val g = 357.529 + 0.98560028 * d
            val q = 280.459 + 0.98564736 * d
            val l = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))
            val e = 23.439 - 0.00000036 * d
            return Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        }
        
        private fun calculateTime(angle: Double, eqt: Double, decl: Double, adjustment: Int, isFajr: Boolean = true): String {
            val lat = Math.toRadians(LATITUDE)
            val d = Math.toRadians(decl)
            val a = if (isFajr) -angle else angle
            
            val cosH = (sin(Math.toRadians(a)) - sin(lat) * sin(d)) / (cos(lat) * cos(d))
            val h = Math.toDegrees(acos(cosH)) / 15.0
            
            val time = if (isFajr) 12 - h else 12 + h
            val finalTime = time - LONGITUDE / 15.0 - eqt + TIMEZONE + (adjustment / 60.0)
            
            return formatTime(finalTime)
        }
        
        private fun calculateDhuhr(eqt: Double, adjustment: Int): String {
            val time = 12 - LONGITUDE / 15.0 - eqt + TIMEZONE + (adjustment / 60.0)
            return formatTime(time)
        }
        
        private fun calculateAsr(eqt: Double, decl: Double, adjustment: Int): String {
            val lat = Math.toRadians(LATITUDE)
            val d = Math.toRadians(decl)
            
            // Shafi'i: shadow = 1 + object length
            val a = atan(1.0 + tan(lat - d))
            val cosH = (sin(a) - sin(lat) * sin(d)) / (cos(lat) * cos(d))
            val h = Math.toDegrees(acos(cosH)) / 15.0
            
            val time = 12 + h - LONGITUDE / 15.0 - eqt + TIMEZONE + (adjustment / 60.0)
            return formatTime(time)
        }
        
        private fun calculateMaghrib(eqt: Double, decl: Double, adjustment: Int): String {
            val lat = Math.toRadians(LATITUDE)
            val d = Math.toRadians(decl)
            
            // Sunset angle: 0.833 degrees below horizon
            val cosH = (sin(Math.toRadians(-0.833)) - sin(lat) * sin(d)) / (cos(lat) * cos(d))
            val h = Math.toDegrees(acos(cosH)) / 15.0
            
            val time = 12 + h - LONGITUDE / 15.0 - eqt + TIMEZONE + (adjustment / 60.0)
            return formatTime(time)
        }
        
        private fun formatTime(time: Double): String {
            var t = time
            if (t < 0) t += 24
            if (t >= 24) t -= 24
            
            val hours = floor(t).toInt()
            val minutes = floor((t - hours) * 60).toInt()
            
            return String.format("%02d:%02d", hours, minutes)
        }
    }
}
