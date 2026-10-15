package com.bismarck.voleimanager.app.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppDateTimeTest {

    @Test
    fun matchDateTime_roundTrips() {
        val millis = assertNotNull(AppDateTime.parseMatchDateTime("05/03/2024 09:07"))
        assertEquals("05/03/2024 09:07", AppDateTime.formatMatchDateTime(millis))
        assertEquals("2024-03-05", AppDateTime.formatIsoDate(millis))
        assertEquals("09:07", AppDateTime.formatTime(millis))
    }

    @Test
    fun parsers_acceptSingleDigitsAndTrailingText_butRejectInvalidDates() {
        assertEquals(
            AppDateTime.parseMatchDateTime("05/03/2024 09:07"),
            AppDateTime.parseMatchDateTime("5/3/2024 9:07 extra")
        )
        assertNull(AppDateTime.parseMatchDateTime("31/02/2024 10:00"))
        assertNull(AppDateTime.parseMatchDateTime("not a date"))
        assertNull(AppDateTime.parseIsoDate("2024-13-01"))
    }

    @Test
    fun matchDateToIsoDay_convertsHistoryDateToLogDate() {
        assertEquals("2023-12-31", AppDateTime.matchDateToIsoDay("31/12/2023 23:59"))
        assertNull(AppDateTime.matchDateToIsoDay("31/12/2023"))
    }

    @Test
    fun parseLegacyMatchDate_triesAllKnownFormats() {
        val expected = AppDateTime.parseMatchDateTime("10/01/2022 18:30")
        assertEquals(expected, AppDateTime.parseLegacyMatchDate("  10/01/2022 18:30 "))
        assertEquals(expected, AppDateTime.parseLegacyMatchDate("2022-01-10 18:30"))
        assertEquals(AppDateTime.parseIsoDate("2022-01-10"), AppDateTime.parseLegacyMatchDate("10/01/2022"))
        assertEquals(AppDateTime.parseIsoDate("2022-01-10"), AppDateTime.parseLegacyMatchDate("2022-01-10"))
        assertNull(AppDateTime.parseLegacyMatchDate(""))
    }

    @Test
    fun todayAndStartOfToday_areConsistent() {
        val start = AppDateTime.startOfTodayMillis()
        assertTrue(start <= AppDateTime.nowMillis())
        assertEquals(AppDateTime.todayIsoDate(), AppDateTime.formatIsoDate(start))
        assertEquals(start, AppDateTime.parseIsoDate(AppDateTime.todayIsoDate()))
    }

    @Test
    fun sortHistoryDatesNewestFirst_ordersChronologically() {
        assertEquals(
            listOf("02/01/2024", "31/12/2023", "15/06/2023"),
            AppDateTime.sortHistoryDatesNewestFirst(listOf("15/06/2023", "02/01/2024", "31/12/2023"))
        )
    }

    @Test
    fun formatLocalizedDate_onlyChangesEnglish() {
        assertEquals("05/03/2024 15:07", AppDateTime.formatLocalizedDate("05/03/2024 15:07", "pt"))
        assertEquals("March 5, 2024 at 3:07 PM", AppDateTime.formatLocalizedDate("05/03/2024 15:07", "en"))
        assertEquals("March 5, 2024 at 12:00 AM", AppDateTime.formatLocalizedDate("05/03/2024 00:00", "en"))
        assertEquals("December 25, 2023", AppDateTime.formatLocalizedDate("25/12/2023", "en"))
        assertEquals("garbage", AppDateTime.formatLocalizedDate("garbage", "en"))
    }

    @Test
    fun formatTwoDecimals_matchesUsLocaleFormat() {
        assertEquals("1200.00", formatTwoDecimals(1200.0))
        assertEquals("1215.46", formatTwoDecimals(1215.456))
        assertEquals("-3.50", formatTwoDecimals(-3.5))
        assertEquals("0.00", formatTwoDecimals(-0.001))
    }
}
