package parser

import util.*

typealias Parser<T> = State.() -> T

fun <T> parser(state: State.() -> T): Parser<T> = state

class ParseException(var reason: String, var pos: Int, val line: Int, val col: Int) : RuntimeException()
data class ParseResult<T>(val result: T, val remaining: String)

fun <T> Parser<T>.parse(input: String): Result<ParseResult<T>, ParseException> = parse(input, State())

fun <S : State, T> (S.() -> T).parse(input: String, state: S): Result<ParseResult<T>, ParseException> {
    state.input = input
    return try {
        val p: S.() -> T = {
            blanks()
            this@parse.invoke(this) // invoke all the parsers
        }
        Result.Ok(ParseResult(state.p(), state.reminding))
    } catch (e: ParseException) {
        val consumed = input.substring(0, e.pos)
        val line = consumed.count { it == '\n' }
        val col = e.pos - consumed.lastIndexOf('\n')
        Result.Err(ParseException(e.reason, e.pos, line + 1, col))
    }
}

fun <T> Parser<T>.parseToEnd(input: String): Result<ParseResult<T>, ParseException> = parseToEnd(input, State())

fun <S : State, T> (S.() -> T).parseToEnd(input: String, state: S): Result<ParseResult<T>, ParseException> {
    val p: S.() -> T = {
        val res = this@parseToEnd()
        blanks()
        eof()
        res
    }
    return p.parse(input, state)
}

open class State {
    internal lateinit var input: String
    var pos: Int = 0

    val next: Char
        get() = when (pos >= input.length) {
            true -> fail("Expected char, got EOF.")
            false -> input[pos]
        }

    val reminding: String
        get() = when (pos >= input.length) {
            true -> ""
            false -> input.substring(pos)
        }

    fun eof() {
        if (pos < input.length) {
            fail("Expected EOF, got '$next'.")
        }
    }

    fun char(): Char = when (pos >= input.length) {
        true -> fail("Expected char, got EOF.")
        false -> input[pos++]
    }

    fun char(vararg expected: Char): Char = when (next !in expected && expected.isNotEmpty()) {
        true -> fail("Expected on of ${expected.joinToString()}, but got '$next'")
        false -> char()
    }

    fun str(expected: String): String {
        if (pos + expected.length > input.length) {
            fail("Expected '$expected', got EOF.")
        }
        if (!input.regionMatches(pos, expected, 0, expected.length)) {
            fail("Expected '$expected', got '${input.substring(pos, pos + expected.length)}'.")
        }
        pos += expected.length
        return expected
    }

    fun regex(pattern: String): String = regex(Regex(pattern))

    fun regex(pattern: Regex): String {
        val res = pattern.matchAt(input, pos)?.value ?: fail("Expected pattern '$pattern', got no match.")
        pos += res.length
        return res
    }

    fun fail(msg: String): Nothing {
        failure.reason = msg
        failure.pos = pos
        throw failure
    }

    fun propagateLastFailure(newMsg: String? = null): Nothing {
        newMsg?.let { failure.reason = newMsg }
        throw failure
    }

    private val failure: ParseException = ParseException("", 0, 0, 0)
}

private object Regexes {
    val blanks = Regex("\\s*")
    val num = Regex("[+\\-]?[0-9]+")
    val deci = Regex("[+\\-]?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+\\-]?\\d+)?")
    val bool = Regex("(true|false)\\b")
    val doubleQuoted = Regex(""""[^\\"]*(\\["nrtb\\][^\\"]*)*"""")
    val singleQuoted = Regex("""'[^\\']*(\\['nrtb\\][^\\']*)*'""")
}

val blanks: Parser<String> = regex(Regexes.blanks)
val num: Parser<Long> = parser { regex(Regexes.num).toLongOrNull() ?: fail("expected uint64") }
val deci: Parser<Double> = parser { regex(Regexes.deci).toDouble() }
val bool: Parser<Boolean> = parser { regex(Regexes.bool).toBooleanStrict() }
var char: Parser<Char> = parser { char() }
val quotedStr: Parser<String> = quotedString(Regexes.doubleQuoted)
val singleQuotedStr: Parser<String> = quotedString(Regexes.singleQuoted)

private fun quotedString(regex: Regex): Parser<String> =
    regex(regex).map {
        it.substring(1, it.lastIndex)
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\b", "\b")
            .replace("\\'", "'")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

fun regex(pattern: String): Parser<String> = regex(Regex(pattern))
fun regex(pattern: Regex): Parser<String> = parser { regex(pattern) }
fun str(expected: String) = parser { str(expected) }

