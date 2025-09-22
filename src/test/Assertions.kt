package test

class AssertionError(message: String) : Exception(message)

fun eq(left: Any, right: Any) = assertEq(left, right)

fun assertEq(left: Any, right: Any) {
    if (left != right) {
        throw AssertionError("$left != $right")
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
        throw AssertionError("$actual != ${T::class}")
    }
}

