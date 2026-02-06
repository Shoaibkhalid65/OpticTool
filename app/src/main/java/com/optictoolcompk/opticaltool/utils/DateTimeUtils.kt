package com.optictoolcompk.opticaltool.utils


import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateTimeUtils {

    // Date formatters
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

    // Time formatters
    private val displayTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    private val isoTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

    // ==================== CURRENT DATE/TIME ====================

    fun getCurrentDate(): String {
        return LocalDate.now().format(displayDateFormatter)
    }

    fun getCurrentTime(): String {
        return LocalTime.now().format(displayTimeFormatter)
    }

    fun getCurrentDateISO(): String {
        return LocalDate.now().format(isoDateFormatter)
    }

    fun getCurrentTimeISO(): String {
        return LocalTime.now().format(isoTimeFormatter)
    }

    // ==================== DATE FORMATTING ====================

    /**
     * Format ISO date (yyyy-MM-dd) to display format (dd MMMM yyyy)
     * Example: "2025-12-30" -> "30 December 2025"
     */
    fun formatDateForDisplay(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, isoDateFormatter)
            date.format(displayDateFormatter)
        } catch (e: DateTimeParseException) {
            // If already in display format or invalid, return as is
            isoDate
        }
    }

    /**
     * Format display date (dd MMMM yyyy) to ISO (yyyy-MM-dd)
     * Example: "30 December 2025" -> "2025-12-30"
     */
    fun formatDateToISO(displayDate: String): String {
        return try {
            val date = LocalDate.parse(displayDate, displayDateFormatter)
            date.format(isoDateFormatter)
        } catch (e: DateTimeParseException) {
            // If already in ISO format or invalid, return as is
            displayDate
        }
    }

    /**
     * Format date to short display (dd MMM yyyy)
     * Example: "30 December 2025" -> "30 Dec 2025"
     */
    fun formatDateShort(displayDate: String): String {
        return try {
            val date = LocalDate.parse(displayDate, displayDateFormatter)
            date.format(shortDateFormatter)
        } catch (e: DateTimeParseException) {
            displayDate
        }
    }

    // ==================== TIME FORMATTING ====================

    /**
     * Format ISO time (HH:mm) to display format (hh:mm a)
     * Example: "14:30" -> "02:30 PM"
     */
    fun formatTimeForDisplay(isoTime: String): String {
        return try {
            val time = LocalTime.parse(isoTime, isoTimeFormatter)
            time.format(displayTimeFormatter)
        } catch (e: DateTimeParseException) {
            isoTime
        }
    }

    /**
     * Format display time (hh:mm a) to ISO (HH:mm)
     * Example: "02:30 PM" -> "14:30"
     */
    fun formatTimeToISO(displayTime: String): String {
        return try {
            val time = LocalTime.parse(displayTime, displayTimeFormatter)
            time.format(isoTimeFormatter)
        } catch (e: DateTimeParseException) {
            displayTime
        }
    }

    // ==================== VALIDATION ====================

    fun isValidDate(dateString: String): Boolean {
        return try {
            LocalDate.parse(dateString, isoDateFormatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isValidDisplayDate(dateString: String): Boolean {
        return try {
            LocalDate.parse(dateString, displayDateFormatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    // ==================== COMPARISON ====================

    /**
     * Compare two dates in display format
     * Returns: negative if date1 < date2, zero if equal, positive if date1 > date2
     */
    fun compareDates(date1: String, date2: String): Int {
        return try {
            val d1 = LocalDate.parse(date1, displayDateFormatter)
            val d2 = LocalDate.parse(date2, displayDateFormatter)
            d1.compareTo(d2)
        } catch (e: DateTimeParseException) {
            0
        }
    }

    /**
     * Check if date is in range (inclusive)
     */
    fun isDateInRange(date: String, startDate: String?, endDate: String?): Boolean {
        if (startDate == null && endDate == null) return true

        return try {
            val d = LocalDate.parse(date, displayDateFormatter)

            val startCheck = startDate?.let {
                val start = LocalDate.parse(it, displayDateFormatter)
                !d.isBefore(start)
            } ?: true

            val endCheck = endDate?.let {
                val end = LocalDate.parse(it, displayDateFormatter)
                !d.isAfter(end)
            } ?: true

            startCheck && endCheck
        } catch (e: DateTimeParseException) {
            true
        }
    }
}