package util

interface Into<T> {
    fun into(): T
}

fun <T> List<T>.into(): List<T> where T: Into<T> = map { it.into() }
