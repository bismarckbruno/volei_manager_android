package com.bismarck.voleimanager.app.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Datas/horas multiplataforma (substitui `SimpleDateFormat`/`Date`/`Calendar`), sempre no fuso
 * local. Os formatos persistidos são fixos e numéricos:
 * - `yyyy-MM-dd` — [com.bismarck.voleimanager.app.data.model.PlayerEloLog.date], `tollDate`;
 * - `dd/MM/yyyy HH:mm` — [com.bismarck.voleimanager.app.data.model.MatchHistory.date].
 *
 * Os parsers aceitam campos de 1 ou 2 dígitos e ignoram texto após o padrão (como o
 * `SimpleDateFormat.parse` fazia), mas rejeitam datas inexistentes.
 */
object AppDateTime {
    private val timeZone: TimeZone get() = TimeZone.currentSystemDefault()

    private val matchDateTimeRegex = Regex("""^(\d{1,2})/(\d{1,2})/(\d{4}) (\d{1,2}):(\d{1,2})""")
    private val displayDateRegex = Regex("""^(\d{1,2})/(\d{1,2})/(\d{4})""")
    private val isoDateTimeRegex = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2}) (\d{1,2}):(\d{1,2})""")
    private val isoDateRegex = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})""")

    private val englishMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    private fun toLocal(millis: Long): LocalDateTime =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(timeZone)

    private fun Int.pad2(): String = toString().padStart(2, '0')

    /** `yyyy-MM-dd`. */
    fun formatIsoDate(millis: Long = nowMillis()): String {
        val d = toLocal(millis)
        return "${d.year.toString().padStart(4, '0')}-${d.month.ordinal.plus(1).pad2()}-${d.day.pad2()}"
    }

    fun todayIsoDate(): String = formatIsoDate()

    /** `dd/MM/yyyy HH:mm`. */
    fun formatMatchDateTime(millis: Long = nowMillis()): String {
        val d = toLocal(millis)
        return "${d.day.pad2()}/${d.month.ordinal.plus(1).pad2()}/${d.year.toString().padStart(4, '0')} " +
            "${d.hour.pad2()}:${d.minute.pad2()}"
    }

    /** `HH:mm`. */
    fun formatTime(millis: Long): String {
        val d = toLocal(millis)
        return "${d.hour.pad2()}:${d.minute.pad2()}"
    }

    fun startOfTodayMillis(): Long {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        return today.atStartOfDayIn(timeZone).toEpochMilliseconds()
    }

    private fun build(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long? =
        runCatching {
            LocalDateTime(year, month, day, hour, minute).toInstant(timeZone).toEpochMilliseconds()
        }.getOrNull()

    private fun MatchResult.ints(): List<Int> = groupValues.drop(1).map { it.toInt() }

    /** `dd/MM/yyyy HH:mm` → epoch millis. */
    fun parseMatchDateTime(text: String): Long? =
        matchDateTimeRegex.find(text)?.ints()?.let { (d, m, y, h, min) -> build(y, m, d, h, min) }

    /** `dd/MM/yyyy` (meia-noite local) → epoch millis. */
    fun parseDisplayDate(text: String): Long? =
        displayDateRegex.find(text)?.ints()?.let { (d, m, y) -> build(y, m, d) }

    /** `yyyy-MM-dd` (meia-noite local) → epoch millis. */
    fun parseIsoDate(text: String): Long? =
        isoDateRegex.find(text)?.ints()?.let { (y, m, d) -> build(y, m, d) }

    /** `dd/MM/yyyy HH:mm` → `yyyy-MM-dd` (dia da partida no formato dos logs de Elo). */
    fun matchDateToIsoDay(text: String): String? =
        parseMatchDateTime(text)?.let { formatIsoDate(it) }

    /** Datas `dd/MM/yyyy` (filtros do histórico) da mais recente para a mais antiga. */
    fun sortHistoryDatesNewestFirst(dates: List<String>): List<String> =
        dates.sortedBy { parseDisplayDate(it) ?: Long.MIN_VALUE }.reversed()

    /** Datas de partidas antigas, que podem não ter `startTimestamp`/`endTimestamp`. */
    fun parseLegacyMatchDate(rawDate: String): Long? {
        val text = rawDate.trim()
        if (text.isBlank()) return null
        return parseMatchDateTime(text)
            ?: parseDisplayDate(text)
            ?: isoDateTimeRegex.find(text)?.ints()?.let { (y, m, d, h, min) -> build(y, m, d, h, min) }
            ?: parseIsoDate(text)
    }

    /**
     * Data interna (`dd/MM/yyyy HH:mm` ou `dd/MM/yyyy`) para exibição. Só o inglês muda o formato
     * (`March 5, 2024 at 3:07 PM`); os demais idiomas já usam a forma numérica interna.
     */
    fun formatLocalizedDate(internalDate: String, languageCode: String): String {
        if (languageCode != "en") return internalDate
        val withTime = internalDate.contains(":")
        val millis = (if (withTime) parseMatchDateTime(internalDate) else parseDisplayDate(internalDate))
            ?: return internalDate
        val d = toLocal(millis)
        val date = "${englishMonths[d.month.ordinal]} ${d.day}, ${d.year}"
        if (!withTime) return date
        val hour12 = if (d.hour % 12 == 0) 12 else d.hour % 12
        val amPm = if (d.hour < 12) "AM" else "PM"
        return "$date at $hour12:${d.minute.pad2()} $amPm"
    }
}
