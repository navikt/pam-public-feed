package no.nav.pam.feed.ad


/**
 * Parse potentially negated and comma-separated multi-value parameter.
 *
 * @param name
 * @return empty optional if no values, parsed param values otherwise
 */
fun String.parseAsValueFilter(name: String): ValueParam? {
    if(this.trim().parsedValue.isEmpty()) return null
    var prefix = ""
    if ("orgnr".equals(name))
        prefix = "employer."

    return ValueParam(prefix + name, this)
}

/**
 * Parse potentially negated and comma-separated multi-value parameter.
 *
 * @param name
 * @return empty optional if no values, parsed param values otherwise
 */
fun String.parseAsLocationValueFilter(name: String): ValueParam? {
    if(this.trim().parsedValue.isEmpty()) return null
    val esName = if (listOf("municipal", "county").contains(name))
                        "locationList.${name}.keyword"
                else name

    return ValueParam(esName, this.toUpperCase())
}

/**
 * Parse generic value parameter, potentially negated and/or multi-value
 */
class ValueParam internal constructor(val name: String, value: String) {

    private val values: List<String>

    init {
        require(value.parsedValue.isNotBlank()) {"values cannot be empty"}
        values = splitCsvParamValue(value.parsedValue)
    }

    val isNegated = value.negated

    fun value() = values[0]

    fun values() = values

    private fun splitCsvParamValue(paramValue: String): List<String> {
        return paramValue.split(MULTI_VALUE_SEPARATOR).dropLastWhile { it.isEmpty() }.toTypedArray()
                .map {  v -> v.trim { it <= ' ' }  }
                .filter { v -> v.isNotEmpty() }
    }
}

private val MULTI_VALUE_SEPARATOR = ",".toRegex()
private val String.negated get() = this.startsWith("!")
private val String.parsedValue get() = if(negated) substring(1) else this
