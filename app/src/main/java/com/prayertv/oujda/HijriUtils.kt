package com.prayertv.oujda

import java.util.*

/**
 * حساب التاريخ الهجري
 */
class HijriUtils {
    
    companion object {
        fun getHijriDate(calendar: Calendar): String {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            return gregorianToHijri(year, month, day)
        }
        
        private fun gregorianToHijri(gYear: Int, gMonth: Int, gDay: Int): String {
            // Convert Gregorian to Julian Day Number
            var a = (14 - gMonth) / 12
            var y = gYear + 4800 - a
            var m = gMonth + 12 * a - 3
            var jdn = gDay + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
            
            // Convert Julian Day Number to Hijri
            val l = jdn - 1948440 + 10632
            val n = (l - 1) / 10631
            val l2 = l - 10631 * n + 354
            val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
            val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
            
            val hMonth = ((24 * l3) / 709).toInt()
            val hDay = (l3 - ((709 * hMonth) / 24)).toInt()
            val hYear = (30 * n + j - 30).toInt()
            
            val monthNames = arrayOf(
                "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
                "جمادى الأولى", "جمادى الثانية", "رجب", "شعبان",
                "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
            )
            
            return "$hDay ${monthNames[hMonth - 1]} $hYear هـ"
        }
        
        fun isRamadan(calendar: Calendar): Boolean {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            val hijriDate = gregorianToHijri(year, month, day)
            return hijriDate.contains("رمضان")
        }
    }
}
