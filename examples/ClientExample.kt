import client.*
import json.*
import kotlinx.coroutines.*
import server.*
import util.*
import java.net.*

fun main() {
    Logger.load("/log.conf")
    val log = logger("example")

    Client(JsonSerde).use { client ->
        val response = runBlocking {
            client.get(URI("http://localhost:8080/persons/1")) {
                setHeader("Content-Type", ContentType.Json.withCharsetUtf8())
                setHeader("Accept", ContentType.Json.value)
            }
        }

        response.onSuccess {
            log.info(it)
        }.onFailure {
            log.error(it)
        }
    }
}
