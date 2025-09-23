import coroutine.*

fun main() = runBlocking {
    println("hello coroutine")
    val result = coroutine { 
        "hello"
    }
    println("continue - completed with $result")
}

