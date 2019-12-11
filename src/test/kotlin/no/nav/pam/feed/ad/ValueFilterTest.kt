package no.nav.pam.feed.ad


import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ValueFilterTest {

    @Test
    fun valueParam() {
        val p = "val".parseAsValueFilter("foo")
        Assertions.assertThat(p).isNotNull()
        Assertions.assertThat(p?.isNegated).isFalse()
        Assertions.assertThat(p?.name).isEqualTo("foo")
        Assertions.assertThat(p?.value()).isEqualTo("val")
        Assertions.assertThat<String>(p?.values()).containsExactly("val")
    }

    @Test
    fun valueParamTrim() {
        val p = "val".parseAsValueFilter("foo")
        Assertions.assertThat(p).isNotNull()
        Assertions.assertThat(p?.value()).isEqualTo("val")
    }

    @Test
    fun negatedValueParam() {
        val p = "!val".parseAsValueFilter("foo")
        Assertions.assertThat(p).isNotNull()
        Assertions.assertThat(p?.isNegated).isTrue()
        Assertions.assertThat(p?.value()).isEqualTo("val")
    }

    @Test
    fun multiValueParam() {
        val p = "0 bad, 1 ok,2 very good ".parseAsValueFilter("foo")
        Assertions.assertThat(p).isNotNull()
        Assertions.assertThat(p?.isNegated).isFalse()
        Assertions.assertThat<String>(p?.values()).containsExactly("0 bad", "1 ok", "2 very good")
    }


}