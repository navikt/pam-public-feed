package no.nav.pam.feed.ad


import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.regex.Pattern

/**
 * Parse data parameter value, either as single date or interval expression.
 * @param name parameter name
 * @param supportInterval whether to attempt parse as date interval, before fallback to single date
 * @return a parsed date param, or empty optional if value is completely empty
 * @throws IllegalArgumentException in case of unparseable date(s)
 */
fun String.parseAsDateFilter(name: String, supportInterval: Boolean): Optional<DateParam> {
    if(trim().isEmpty()) return Optional.empty()
    val dateParam = if(isIntervalExpression && supportInterval) IntervalDateParam(name, this) else SingleDateParam(name, this)
    return Optional.of(dateParam)
}

/**
 * Converter that transform a
 */
interface Converter<T> {

    fun convert(ta: TemporalAccessor): T

    object ToString: Converter<String> {
        override fun convert(ta: TemporalAccessor): String {
            return if(ta.isSupported(ChronoField.HOUR_OF_DAY)) DateTimeFormatter.ISO_DATE_TIME.format(ta) else DateTimeFormatter.ISO_DATE.format(ta)
        }

    }

    object ToLocalDateTime:Converter<LocalDateTime> {

        override fun convert(ta: TemporalAccessor): LocalDateTime {
            return if(ta.isSupported(ChronoField.HOUR_OF_DAY)) LocalDateTime.from(ta) else LocalDate.from(ta).atStartOfDay()
        }
    }
}

/**
 * Represents a date filter
 */
interface DateParam {

    fun name(): String

    fun startInclusive(): Boolean

    fun endInclusive(): Boolean

    fun <T> start(converter: Converter<T>): T

    fun <T> end(converter: Converter<T>): T

}


private val DATE_INTERVAL_SYNTAX = Pattern.compile("([\\[(])([^]),]+)\\s*,\\s*([^])]+)([])])")
private const val DATE_INTERVAL_WILDCARD = "*"

private val startOfTime = LocalDateTime.now().minusYears(100)
private val endOfTime = LocalDateTime.now().plusYears(100)

private val String.isIntervalExpression: Boolean get() = DATE_INTERVAL_SYNTAX.matcher(trim()).matches()
private val String.isWildcard: Boolean get() = this == DATE_INTERVAL_WILDCARD
private val dateTimeFormatter = DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
        .toFormatter()

private fun String.toStartDate(): TemporalAccessor = if(isWildcard) startOfTime else toDate()
private fun String.toEndDate(): TemporalAccessor = if(isWildcard) endOfTime else toDate()
private fun String.toDate(): TemporalAccessor {
    if(this == "now") return LocalDateTime.now()
    if(this == "today") return LocalDate.now()
    if(this == "yesterday") return LocalDate.now().minusDays(1)
    if(this == "tomorrow") return LocalDate.now().plusDays(1)
    return dateTimeFormatter.parse(this)
}

private class SingleDateParam(private val name: String, private val exp: String) : DateParam {

    override fun name() = name

    override fun startInclusive() = true

    override fun endInclusive() = true

    private fun startTime(): TemporalAccessor = exp.toDate()

    private fun endTime(): TemporalAccessor = exp.toDate()

    override fun <T> start(converter: Converter<T>) = startTime().let { converter.convert(it) }

    override fun <T> end(converter: Converter<T>) = endTime().let { converter.convert(it) }

}

class IntervalDateParam(private val name: String, exp: String) : DateParam {

    private val intervalMatcher = DATE_INTERVAL_SYNTAX.matcher(exp.trim())

    private val start: TemporalAccessor
    private val end: TemporalAccessor

    init {
        require(intervalMatcher.matches())
        start = intervalMatcher.group(2).trim().toStartDate()
        end = intervalMatcher.group(3).trim().toEndDate()
    }

    override fun name() = name

    override fun startInclusive() = intervalMatcher.group(1) == "["

    override fun endInclusive() = intervalMatcher.group(4) == "]"

    override fun <T> start(converter: Converter<T>) = converter.convert(start)

    override fun <T> end(converter: Converter<T>) = converter.convert(end)

}
