package parser

data class Chain<T, S>(val terms: List<T>, val separators: List<S>) {
    companion object {
        private val emptyChain: Chain<*, *> = Chain<Any?, Any?>(emptyList(), emptyList())

        @Suppress("UNCHECKED_CAST")
        fun <T, S> empty(): Chain<T, S> = emptyChain as Chain<T, S>
    }
}

inline fun <T, U> Parser<T>.map(crossinline f: (T) -> U): Parser<U> = parser { f(this.this@map()) }
inline fun <T, U> Parser<T>.then(crossinline p: Parser<U>): Parser<Pair<T, U>> = parser { this@then() to p() }
inline fun <T> many(crossinline p: Parser<T>) = parser { many(p) }
fun <T> many1(p: Parser<T>) = parser { many1(p) }
fun <T> oneOf(vararg ps: Parser<T>) = parser { oneOf(*ps) }
fun <T> oneOf(vararg ps: Pair<String, Parser<T>>) = parser { oneOf(*ps) }
inline fun <T, S> chain(crossinline p: Parser<T>, crossinline separator: Parser<S>) = parser { chain(p, separator) }
inline fun <T, S> chain1(crossinline p: Parser<T>, crossinline separator: Parser<S>) = parser { chain1(p, separator) }
inline fun <T> atomically(crossinline p: Parser<T>): Parser<T> = parser { atomically(p) }

inline fun <T> State.many(crossinline p: Parser<T>): List<T> {
    val list = mutableListOf<T>()
    var savedPos = 0
    while (true) {
        try {
            savedPos = pos
            list.add(p())
        } catch (e: ParseException) {
            pos = savedPos
            return list
        }
    }
}

inline fun <T> State.many1(crossinline p: Parser<T>): List<T> = atomically {
    val elems = many(p)
    when (elems.isEmpty()) {
        true -> fail("Expected at least one element, but got none.")
        false -> return elems
    }
}


fun <T> State.oneOf(vararg parsers: Parser<T>): T {
    val savedPos = pos
    for (parser in parsers) {
        try {
            return parser()
        } catch (e: ParseException) {
            pos = savedPos
        }
    }
    propagateLastFailure()
}

fun <T> State.oneOf(vararg parsers: Pair<String, Parser<T>>): T {
    val savedPos = pos
    for ((_, parser) in parsers) {
        try {
            return parser()
        } catch (e: ParseException) {
            pos = savedPos
        }
    }
    val alts = parsers.joinToString { (it.first) }
    fail("Expected one of $alts.")
}

inline fun <T, S> State.chain1(crossinline p: Parser<T>, crossinline separator: Parser<S>): Chain<T, S> =
    atomically {
        val head = p()
        val tail = many {
            separator() to p()
        }
        val separators = mutableListOf<S>()
        val terms = mutableListOf(head)
        for ((s, t) in tail) {
            separators.add(s)
            terms.add(t)
        }
        Chain(terms, separators)
    }

inline fun <T, S> State.chain(crossinline p: Parser<T>, crossinline separator: Parser<S>): Chain<T, S> =
    oneOf(
        parser { chain1(p, separator) },
        parser { Chain.empty() }
    )

inline fun <T, S> State.chainl(
    crossinline p: Parser<T>,
    crossinline separator: Parser<S>,
    crossinline combine: (T, T, S) -> T
): T {
    val c = chain(p, separator)
    var result: T = c.terms.firstOrNull() ?: fail("Chain did not match any elements.")
    for (i in c.separators.indices) {
        result = combine(result, c.terms[i + 1], c.separators[i])
    }
    return result
}

inline fun <T, S> chainl(
    crossinline p: Parser<T>,
    crossinline separator: Parser<S>,
    crossinline combine: (T, T, S) -> T
) = parser { chainl(p, separator, combine) }

inline fun <T, S> State.chainr(
    crossinline p: Parser<T>,
    crossinline separator: Parser<S>,
    crossinline combine: (T, T, S) -> T
): T {
    val c = chain(p, separator)
    var result: T = c.terms.lastOrNull() ?: fail("Chain did not match any elements.")
    for (i in (c.separators.size - 1) downTo 0) {
        result = combine(c.terms[i], result, c.separators[i])
    }
    return result
}

inline fun <T, S> chainr(
    crossinline p: Parser<T>,
    crossinline separator: Parser<S>,
    crossinline combine: (T, T, S) -> T
) = parser { chainr(p, separator, combine) }

inline fun <B, T> State.surround(
    crossinline before: Parser<B>,
    crossinline after: Parser<B>,
    crossinline p: Parser<T>
): T = atomically {
    before()
    val x = p()
    after()
    x
}

inline fun <B, T> surround(
    crossinline before: Parser<B>,
    crossinline after: Parser<B>,
    crossinline p: Parser<T>
) = parser { surround(before, after, p) }

inline fun <T> State.atomically(p: Parser<T>): T {
    val savedPos = pos
    return try {
        p()
    } catch (e: ParseException) {
        pos = savedPos
        e.pos = savedPos
        throw e
    }
}

