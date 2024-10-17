package json

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import parser.*
import util.*

class TokensTest {

    @Nested
    inner class Digit {

    }

    @Nested
    inner class Decimal {

    }

    @Nested
    inner class Bool {
        @Test
        fun `can parse true`() {
            val parsed = bool.parseToEnd("true").unwrap()
            assertInstanceOf<ParseResult<Boolean>>(parsed)
            assertEquals(true, parsed.result)
        }

        @Test
        fun `can parse false`() {
            val parsed = bool.parseToEnd("false").unwrap()
            assertInstanceOf<ParseResult<Boolean>>(parsed)
            assertEquals(false, parsed.result)
        }

        @Test
        fun `cannot parse garbage`() {
            assertInstanceOf<ParseException>(bool.parse("rue").unwrapErr())
            assertInstanceOf<ParseException>(bool.parse("tru").unwrapErr())
            assertInstanceOf<ParseException>(bool.parse("truee").unwrapErr())
            assertInstanceOf<ParseException>(bool.parse("ttrue").unwrapErr())
            assertInstanceOf<ParseException>(bool.parse("lfalsel").unwrapErr())
            assertInstanceOf<ParseException>(bool.parse("1").unwrapErr())
            assertInstanceOf<ParseException>(bool.parse("$").unwrapErr())
        }
    }

    @Nested
    inner class DoubleQuoted {

    }

    @Nested
    inner class SingleQuoted {

    }
}
