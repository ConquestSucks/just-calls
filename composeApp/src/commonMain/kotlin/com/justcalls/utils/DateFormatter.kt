package com.justcalls.utils

object DateFormatter {
    fun formatDate(isoDateString: String): String {
        return try {
            val dateTimePart = isoDateString.split("T")
            if (dateTimePart.size != 2) return isoDateString
            
            val datePart = dateTimePart[0].split("-")
            if (datePart.size != 3) return isoDateString
            
            val timePart = dateTimePart[1].split(":").map { it.split(".")[0] }
            if (timePart.size < 2) return isoDateString
            
            val year = datePart[0].toIntOrNull() ?: return isoDateString
            val month = datePart[1].toIntOrNull() ?: return isoDateString
            val day = datePart[2].toIntOrNull() ?: return isoDateString
            val hour = timePart[0].toIntOrNull() ?: 0
            val minute = timePart[1].toIntOrNull() ?: 0
            
            val months = listOf(
                "января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"
            )
            
            val monthName = months.getOrNull(month - 1) ?: ""
            val hourStr = hour.toString().padStart(2, '0')
            val minuteStr = minute.toString().padStart(2, '0')
            
            "$day $monthName $year г. в $hourStr:$minuteStr"
        } catch (e: Exception) {
            isoDateString
        }
    }
}

