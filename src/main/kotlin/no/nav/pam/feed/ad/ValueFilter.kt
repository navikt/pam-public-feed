package no.nav.pam.feed.ad

import java.util.*


/**
 * Parse potentially negated and comma-separated multi-value parameter.
 *
 * @param name
 * @param trim
 * @return empty optional if no values, parsed param values otherwise
 */
fun String.parseAsValueFilter(name: String, trim: Boolean): Optional<ValueParam> {
    if(this.trim(trim).parsedValue.isEmpty()) return Optional.empty()

    return Optional.of(ValueParam(name, this, trim))
}

/**
 * Parse generic value parameter, potentially negated and/or multi-value
 */
class ValueParam internal constructor(val name: String, value: String, trim: Boolean) {

    private val values: List<String>

    init {
        require(value.parsedValue.isNotBlank()) {"values cannot be empty"}
        values = splitCsvParamValue(value.parsedValue, trim)
    }

    val isNegated = value.negated

    fun value() = values[0]

    fun values() = values

    private fun splitCsvParamValue(paramValue: String, trim: Boolean): List<String> {
        return paramValue.split(MULTI_VALUE_SEPARATOR.toString().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                .map {  v -> if (trim) v.trim { it <= ' ' } else v  }
                .filter { v -> !v.isEmpty() }
    }
}

private const val MULTI_VALUE_SEPARATOR = ','
private val String.negated get() = this.startsWith("!")
private val String.parsedValue get() = if(negated) substring(1) else this
private fun String.trim(trim: Boolean) = if(trim) trim() else this
