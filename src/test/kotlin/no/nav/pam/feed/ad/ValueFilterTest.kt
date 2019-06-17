package no.nav.pam.feed.ad


import org.assertj.core.api.Assertions
import org.junit.Test

class ValueFilterTest {

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


}