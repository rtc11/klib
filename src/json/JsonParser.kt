package json

import parser.*
import util.*

typealias Json = Map<String, Any?>

object JsonParser {
    private val token = oneOf(deci, quotedStr, bool, str("null").map { null })
    private val comma = parser { blanks(); char(','); blanks() }
    private val colon = parser { blanks(); char(':'); blanks() }
    private val keyValue = parser { quotedStr().also { colon() } to value() }
    private val array = surround(
        parser { char('['); blanks() },
        parser { blanks(); char(']') },
        parser { chain(value, comma).terms },
    )
    private val obj = surround(
        parser { char('{'); blanks() },
        parser { blanks(); char('}') },
        parser { chain(keyValue, comma).terms.toMap() },
    )
    private val value: Parser<Any?> = oneOf(token, obj, array)

    @Suppress("UNCHECKED_CAST")
    fun parse(input: String): Result<ParseResult<Json>, ParseException> {
        val parsed = value.parseToEnd(input)
        return parsed as Result<ParseResult<Json>, ParseException>
    }
}
