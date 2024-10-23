package test

class AssertionError(message: String) : Exception(message)

fun assertEq(expected: Any, actual: Any) {
    if (expected != actual) {
        throw AssertionError("Expected $expected but got $actual")
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

// todo: this only fails with "assertion failed". Make it fail with a message that includes the expected and actual values
inline fun <reified T: Any> assert(it: Any, predicate: (T) -> Boolean) {
    assert(predicate(it as T))
}

inline fun <reified T: Any> assertIs(actual: Any) {
    if (actual !is T) {
        throw AssertionError("Expected $actual to be of type ${T::class}")
    }
}

