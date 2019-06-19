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
    val dateParam = if(isIntervalExpression() && supportInterval) IntervalDateParam(name, this) else SingleDateParam(name, this)
    return Optional.of(dateParam)
}


val stringConverter = fun(ta: TemporalAccessor) =
        if(ta.isSupported(ChronoField.HOUR_OF_DAY)) DateTimeFormatter.ISO_DATE_TIME.format(ta) else DateTimeFormatter.ISO_DATE.format(ta)

val dateTimeConverter = fun (ta: TemporalAccessor) =
        if(ta.isSupported(ChronoField.HOUR_OF_DAY)) LocalDateTime.from(ta) else LocalDate.from(ta).atStartOfDay()

/**
 * Represents a date filter
 */
interface DateParam {

    val name: String

    val startInclusive: Boolean

    val endInclusive: Boolean

    fun <T> start(converter: (TemporalAccessor) -> T): T

    fun <T> end(converter: (TemporalAccessor) -> T): T

}


private val DATE_INTERVAL_SYNTAX = Pattern.compile("([\\[(])([^]),]+)\\s*,\\s*([^])]+)([])])")
private const val wildcard = "*"
private val startOfTime = LocalDateTime.now().minusYears(100)
private val endOfTime = LocalDateTime.now().plusYears(100)
private val dateTimeFormatter = DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
        .toFormatter()

private fun String.isIntervalExpression(): Boolean = DATE_INTERVAL_SYNTAX.matcher(trim()).matches()
private fun String.toStartDate(): TemporalAccessor = if(this == wildcard) startOfTime else toDate()
private fun String.toEndDate(): TemporalAccessor = if(this == wildcard) endOfTime else toDate()
private fun String.toDate(): TemporalAccessor {
    if(this == "now") return LocalDateTime.now()
    if(this == "today") return LocalDate.now()
    if(this == "yesterday") return LocalDate.now().minusDays(1)
    if(this == "tomorrow") return LocalDate.now().plusDays(1)
    return dateTimeFormatter.parse(this)
}

private class SingleDateParam(override val name: String, private val exp: String) : DateParam {

    override val startInclusive = true
    override val endInclusive = true

    private val startTime get() = exp.toStartDate()
    private val endTime get() = exp.toEndDate()

    override fun <T> start(converter: (TemporalAccessor) -> T) = startTime.let { converter.invoke(it) }

    override fun <T> end(converter: (TemporalAccessor) -> T) = endTime.let { converter.invoke(it) }

}

class IntervalDateParam(override val name: String, exp: String) : DateParam {

    private val intervalMatcher = DATE_INTERVAL_SYNTAX.matcher(exp.trim())
            .apply { require(this.matches()) }

    override val startInclusive get() = intervalMatcher.group(1) == "["
    override val endInclusive get() = intervalMatcher.group(4) == "]"

    private val start get() = intervalMatcher.group(2).trim().toStartDate()
    private val end get() = intervalMatcher.group(3).trim().toEndDate()

    override fun <T> start(converter: (TemporalAccessor) -> T) = converter.invoke(start)

    override fun <T> end(converter: (TemporalAccessor) -> T) = converter.invoke(end)

}
