package json

import parser.*
import test.*
import util.*

class TokensTest {

    @Test
    fun `can parse true`() {
        val parsed = bool.parseToEnd("true").unwrap()
        assertIs<ParseResult<Boolean>>(parsed)
        eq(true, parsed.result)
    }

    @Test
    fun `can parse false`() {
        val parsed = bool.parseToEnd("false").unwrap()
        assertIs<ParseResult<Boolean>>(parsed)
        eq(false, parsed.result)
    }

    @Test
    fun `cannot parse garbage`() {
        assertIs<ParseException>(bool.parse("rue").unwrapErr())
        assertIs<ParseException>(bool.parse("tru").unwrapErr())
        assertIs<ParseException>(bool.parse("truee").unwrapErr())
        assertIs<ParseException>(bool.parse("ttrue").unwrapErr())
        assertIs<ParseException>(bool.parse("lfalsel").unwrapErr())
        assertIs<ParseException>(bool.parse("1").unwrapErr())
        assertIs<ParseException>(bool.parse("$").unwrapErr())
    }
}
