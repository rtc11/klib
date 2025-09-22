package test

class AssertionError(
    message: String,
    val left: Any? = null,
    val right: Any? = null,
) : Exception(message)

fun eq(left: Any?, right: Any?) {
    if (left != right) {
        throw AssertionError("should be equal", left, right)
    }
}

fun neq(left: Any?, right: Any?) {
    if (left == right) {
        throw AssertionError("should not be equal", left, right)
    }
}

fun assert(predicate: Boolean) {
    if (!predicate) {
        throw AssertionError("Assertion failed")
    }
}

fun fail(msg: String = "Assertion failed") {
    throw AssertionError(msg)
}

inline fun <reified T: Any> assert(it: Any, predicate: (T) -> Boolean) {
    assert(predicate(it as T))
}

inline fun <reified T: Any> assertIs(actual: Any) {
    if (actual !is T) {
        throw AssertionError("$actual != ${T::class}")
    }
}

