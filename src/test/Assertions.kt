package test

class AssertionError(message: String) : Exception(message)

fun assertEq(expected: Any, actual: Any) {
    if (expected != actual) {
        throw AssertionError("Expected $expected but got $actual")
    }
}

inline fun <reified T> assertIs(actual: Any) {
    if (actual !is T) {
        throw AssertionError("Expected $actual to be of type ${T::class}")
    }
}

