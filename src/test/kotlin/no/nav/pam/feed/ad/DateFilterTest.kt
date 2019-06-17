package no.nav.pam.feed.ad


import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class DateFilterTest {
    private val converter = Converter.ToString
    private val dateTimeConverter = Converter.ToLocalDateTime

    @Test
    fun valueParam() {
        val p = "val".parseAsValueFilter("foo", false)
        Assertions.assertThat(p.isPresent()).isTrue()
        Assertions.assertThat(p.get().isNegated).isFalse()
        Assertions.assertThat(p.get().name).isEqualTo("foo")
        Assertions.assertThat(p.get().value()).isEqualTo("val")
        Assertions.assertThat<String>(p.get().values()).containsExactly("val")
    }

    @Test
    fun valueParamTrim() {
        val p = "val".parseAsValueFilter("foo", true)
        Assertions.assertThat(p.isPresent()).isTrue()
        Assertions.assertThat(p.get().value()).isEqualTo("val")
    }

    @Test
    fun negatedValueParam() {
        val p = "!val".parseAsValueFilter("foo", false)
        Assertions.assertThat(p.isPresent()).isTrue()
        Assertions.assertThat(p.get().isNegated).isTrue()
        Assertions.assertThat(p.get().value()).isEqualTo("val")
    }

    @Test
    fun multiValueParam() {
        val p = "0 bad, 1 ok,2 very good ".parseAsValueFilter("foo", true)
        Assertions.assertThat(p.isPresent()).isTrue()
        Assertions.assertThat(p.get().isNegated).isFalse()
        Assertions.assertThat<String>(p.get().values()).containsExactly("0 bad", "1 ok", "2 very good")
    }

    @Test
    fun multiValueParamNegatedNoTrim() {
        val p = "!0 bad, 1 ok,2 very good ".parseAsValueFilter("foo", false)
        Assertions.assertThat(p.isPresent()).isTrue()
        Assertions.assertThat(p.get().isNegated).isTrue()
        Assertions.assertThat<String>(p.get().values()).containsExactly("0 bad", " 1 ok", "2 very good ")
    }

    @Test
    fun emptyDate() {
        val dp = " ".parseAsDateFilter("empty", false)
        assertThat(dp.isPresent()).isFalse()
    }

    @Test
    fun simpleDate() {
        val dp = "2018-01-01". parseAsDateFilter("somedate", false)
        Assertions.assertThat(dp.isPresent()).isTrue()

        val d = dp.get()
        Assertions.assertThat(d.start(converter)).isEqualTo(d.end(converter))
        Assertions.assertThat(d.start(converter).toString()).isEqualTo("2018-01-01")
    }

    @Test
    fun simpleDateIgnoreWhitespace() {
        val dp = "  2018-01-01\t". parseAsDateFilter("somedate", false)
        assertThat(dp.isPresent()).isTrue()
    }

    @Test
    fun simpleDateWithTime() {
        val dp = "2018-01-01T12:00". parseAsDateFilter("somedate", false)
        Assertions.assertThat(dp.isPresent()).isTrue()

        val d = dp.get()
        Assertions.assertThat(d.start(converter)).isEqualTo(d.end(converter))
        Assertions.assertThat(d.start(converter).toString()).isEqualTo("2018-01-01T12:00:00")
    }

    @Test
    fun simpleDateWithMilliseconds() {
        val dp = "2018-01-01T12:00:00.123". parseAsDateFilter("somedate", false)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().start(converter).toString()).isEqualTo("2018-01-01T12:00:00.123")
    }

    @Test
    fun dateIntervalDays() {
        val dp = "[2018-01-01,2018-01-02]". parseAsDateFilter("somedate", true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isTrue()
        Assertions.assertThat(dp.get().endInclusive).isTrue()
        Assertions.assertThat(dp.get().start(converter).toString()).isEqualTo("2018-01-01")
        Assertions.assertThat(dp.get().end(converter).toString()).isEqualTo("2018-01-02")
    }

    @Test
    fun dateIntervalDays_startExclusive() {
        val dp = "(2018-01-01,2018-01-02]". parseAsDateFilter("somedate", true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isFalse()
        Assertions.assertThat(dp.get().endInclusive).isTrue()
    }

    @Test
    fun dateIntervalDays_endExclusive() {
        val dp = "[2018-01-01,2018-01-02)". parseAsDateFilter("somedate", true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isTrue()
        Assertions.assertThat(dp.get().endInclusive).isFalse()
    }

    @Test
    fun dateIntervalDays_bothExclusive() {
        val dp = "(2018-01-01,2018-01-02)". parseAsDateFilter("somedate", true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isFalse()
        Assertions.assertThat(dp.get().endInclusive).isFalse()
    }

    @Test
    fun dateIntervalDays_lowerBoundOpen() {
        val dp = "[*,2018-01-02]". parseAsDateFilter("somedate", true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isTrue()
        Assertions.assertThat(dp.get().endInclusive).isTrue()
        Assertions.assertThat(dp.get().start(dateTimeConverter)).isBefore(LocalDateTime.now().minusYears(50))
        Assertions.assertThat(dp.get().end(converter).toString()).isEqualTo("2018-01-02")
    }

    @Test
    fun dateIntervalDays_upperBoundOpen_exclusive() {
        val dp = "(2018-01-01,*)". parseAsDateFilter("somedate", true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isFalse()
        Assertions.assertThat(dp.get().endInclusive).isFalse()
        Assertions.assertThat(dp.get().start(converter).toString()).isEqualTo("2018-01-01")
        Assertions.assertThat(dp.get().end(dateTimeConverter)).isAfter(LocalDateTime.now().plusYears(50))
    }


    @Test
    fun dateIntervalWithTimes() {
        val dp = "[2018-01-01T12:00:01,2018-01-02T12:00]". parseAsDateFilter("somedate", true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isTrue()
        Assertions.assertThat(dp.get().endInclusive).isTrue()
        Assertions.assertThat(dp.get().start(converter).toString()).isEqualTo("2018-01-01T12:00:01")
        Assertions.assertThat(dp.get().end(converter).toString()).isEqualTo("2018-01-02T12:00:00")
    }

    @Test
    fun dateIntervalIgnoreWhitespace() {
        val dp = " [ 2018-01-01T12:00:01, 2018-01-02T12:00  ]\t". parseAsDateFilter("somedate",  true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isTrue()
        Assertions.assertThat(dp.get().endInclusive).isTrue()
        Assertions.assertThat(dp.get().start(converter).toString()).isEqualTo("2018-01-01T12:00:01")
        Assertions.assertThat(dp.get().end(converter).toString()).isEqualTo("2018-01-02T12:00:00")
    }

    @Test
    fun theWholeOfTimeIncludingBigBangs() {
        val dp = "[*, *]". parseAsDateFilter("somedate",  true)
        Assertions.assertThat(dp.isPresent()).isTrue()
        Assertions.assertThat(dp.get().startInclusive).isTrue()
        Assertions.assertThat(dp.get().endInclusive).isTrue()
        Assertions.assertThat(dp.get().start(dateTimeConverter)).isBefore(LocalDateTime.now().minusYears(50))
        Assertions.assertThat(dp.get().end(dateTimeConverter)).isAfter(LocalDateTime.now().plusYears(50))
    }


}