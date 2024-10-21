package util

import java.io.*
import java.net.*

inline fun <reified T : Any> env(variable: String): T {
    val env = System.getenv(variable) ?: error("missing env $variable")

    return when (val type = T::class) {
        String::class -> env as T
        Int::class -> env.toInt() as T
        Long::class -> env.toLong() as T
        Boolean::class -> env.toBoolean() as T
        URI::class -> URI(env) as T
        else -> error("unsupported env type $type")
    }
}

// TODO: return Result
object Resource {
    fun url(path: String): URL = this::class.java.getResource(path) ?: error("missing resource $path")
    fun input(path: String): InputStream = url(path).openStream()
    fun read(path: String): String = input(path).bufferedReader().use { it.readText() }
}
