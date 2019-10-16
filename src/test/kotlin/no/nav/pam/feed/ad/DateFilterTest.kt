package no.nav.pam.feed.ad


import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat as assertThat

class DateFilterTest {

    @Test
    fun valueParam() {
        val p = "val".parseAsValueFilter("foo", false)
        assertThat(p).isNotNull()
        assertThat(p?.isNegated).isFalse()
        assertThat(p?.name).isEqualTo("foo")
        assertThat(p?.value()).isEqualTo("val")
        assertThat<String>(p?.values()).containsExactly("val")
    }

    @Test
    fun valueParamTrim() {
        val p = "val".parseAsValueFilter("foo", true)
        assertThat(p).isNotNull()
        assertThat(p?.value()).isEqualTo("val")
    }

    @Test
    fun negatedValueParam() {
        val p = "!val".parseAsValueFilter("foo", false)
        assertThat(p).isNotNull()
        assertThat(p?.isNegated).isTrue()
        assertThat(p?.value()).isEqualTo("val")
    }

    @Test
    fun multiValueParam() {
        val p = "0 bad, 1 ok,2 very good ".parseAsValueFilter("foo", true)
        assertThat(p).isNotNull()
        assertThat(p?.isNegated).isFalse()
        assertThat<String>(p?.values()).containsExactly("0 bad", "1 ok", "2 very good")
    }

    @Test
    fun multiValueParamNegatedNoTrim() {
        val p = "!0 bad, 1 ok,2 very good ".parseAsValueFilter("foo", false)
        assertThat(p).isNotNull()
        assertThat(p?.isNegated).isTrue()
        assertThat<String>(p?.values()).containsExactly("0 bad", " 1 ok", "2 very good ")
    }

    @Test
    fun emptyDate() {
        val dp = " ".parseAsDateFilter("empty", false)
        assertThat(dp).isNull()
    }

    @Test
    fun simpleDate() {
        val dp = "2018-01-01". parseAsDateFilter("somedate", false)
        assertThat(dp).isNotNull()

        assertThat(dp?.start(stringConverter)).isEqualTo(dp?.end(stringConverter))
        assertThat(dp?.start(stringConverter).toString()).isEqualTo("2018-01-01")
    }

    @Test
    fun simpleDateIgnoreWhitespace() {
        val dp = "  2018-01-01\t". parseAsDateFilter("somedate", false)
        assertThat(dp).isNotNull()
    }

    @Test
    fun simpleDateWithTime() {
        val dp = "2018-01-01T12:00". parseAsDateFilter("somedate", false)
        assertThat(dp).isNotNull()

        assertThat(dp?.start(stringConverter)).isEqualTo(dp?.end(stringConverter))
        assertThat(dp?.start(stringConverter).toString()).isEqualTo("2018-01-01T12:00:00")
    }

    @Test
    fun simpleDateWithMilliseconds() {
        val dp = "2018-01-01T12:00:00.123". parseAsDateFilter("somedate", false)
        assertThat(dp).isNotNull()
        assertThat(dp?.start(stringConverter).toString()).isEqualTo("2018-01-01T12:00:00.123")
    }

    @Test
    fun dateIntervalDays() {
        val dp = "[2018-01-01,2018-01-02]". parseAsDateFilter("somedate", true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isTrue()
        assertThat(dp?.endInclusive).isTrue()
        assertThat(dp?.start(stringConverter).toString()).isEqualTo("2018-01-01")
        assertThat(dp?.end(stringConverter).toString()).isEqualTo("2018-01-02")
    }

    @Test
    fun dateIntervalDays_startExclusive() {
        val dp = "(2018-01-01,2018-01-02]". parseAsDateFilter("somedate", true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isFalse()
        assertThat(dp?.endInclusive).isTrue()
    }

    @Test
    fun dateIntervalDays_endExclusive() {
        val dp = "[2018-01-01,2018-01-02)". parseAsDateFilter("somedate", true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isTrue()
        assertThat(dp?.endInclusive).isFalse()
    }

    @Test
    fun dateIntervalDays_bothExclusive() {
        val dp = "(2018-01-01,2018-01-02)". parseAsDateFilter("somedate", true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isFalse()
        assertThat(dp?.endInclusive).isFalse()
    }

    @Test
    fun dateIntervalDays_lowerBoundOpen() {
        val dp = "[*,2018-01-02]". parseAsDateFilter("somedate", true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isTrue()
        assertThat(dp?.endInclusive).isTrue()
        assertThat(dp?.start(dateTimeConverter)).isBefore(LocalDateTime.now().minusYears(50))
        assertThat(dp?.end(stringConverter).toString()).isEqualTo("2018-01-02")
    }

    @Test
    fun dateIntervalDays_upperBoundOpen_exclusive() {
        val dp = "(2018-01-01,*)". parseAsDateFilter("somedate", true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isFalse()
        assertThat(dp?.endInclusive).isFalse()
        assertThat(dp?.start(stringConverter).toString()).isEqualTo("2018-01-01")
        assertThat(dp?.end(dateTimeConverter)).isAfter(LocalDateTime.now().plusYears(50))
    }


    @Test
    fun dateIntervalWithTimes() {
        val dp = "[2018-01-01T12:00:01,2018-01-02T12:00]". parseAsDateFilter("somedate", true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isTrue()
        assertThat(dp?.endInclusive).isTrue()
        assertThat(dp?.start(stringConverter).toString()).isEqualTo("2018-01-01T12:00:01")
        assertThat(dp?.end(stringConverter).toString()).isEqualTo("2018-01-02T12:00:00")
    }

    @Test
    fun dateIntervalIgnoreWhitespace() {
        val dp = " [ 2018-01-01T12:00:01, 2018-01-02T12:00  ]\t". parseAsDateFilter("somedate",  true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isTrue()
        assertThat(dp?.endInclusive).isTrue()
        assertThat(dp?.start(stringConverter).toString()).isEqualTo("2018-01-01T12:00:01")
        assertThat(dp?.end(stringConverter).toString()).isEqualTo("2018-01-02T12:00:00")
    }

    @Test
    fun theWholeOfTimeIncludingBigBangs() {
        val dp = "[*, *]". parseAsDateFilter("somedate",  true)
        assertThat(dp).isNotNull()
        assertThat(dp?.startInclusive).isTrue()
        assertThat(dp?.endInclusive).isTrue()
        assertThat(dp?.start(dateTimeConverter)).isBefore(LocalDateTime.now().minusYears(50))
        assertThat(dp?.end(dateTimeConverter)).isAfter(LocalDateTime.now().plusYears(50))
    }


}