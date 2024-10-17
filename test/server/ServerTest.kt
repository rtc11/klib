package server

import client.*
import json.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import serde.*
import util.*
import java.net.*

class ServerTest {
    private data class Name(val name: String) : Into<Json> {
        override fun into(): Json = mapOf(Name::name.name to name)
    }

    private val server = Server(8080).apply {
        router("/") {
            get("text") {
                respond(StatusCode.OK, "hello")
            }
            get("json") {
                respond(StatusCode.OK, Name("robin"))
            }
        }
    }

    @Test
    fun `response is idempotent`() {
        val client = Client(StringSerde)
        server.start(0)

        fun fetchText() = runBlocking {
            client.get(URI("http://localhost:8080/text")) {
                setHeader("Content-Type", ContentType.Text.withCharsetUtf8())
                setHeader("Accept", ContentType.Text.value)
            }
        }

        fun fetchJson() = runBlocking {
            client.get(URI("http://localhost:8080/json")) {
                setHeader("Content-Type", ContentType.Json.withCharsetUtf8())
                setHeader("Accept", ContentType.Json.value)
            }
        }

        val firstText = fetchText().expect("hello")
        val secondText = fetchText().expect("hello")
        val firstJson = fetchJson().expect("Json with name:robin")
        val secondJson = fetchJson().expect("Json with name:robin")

        assertEquals("hello", firstText)
        assertEquals("hello", secondText)
        assertEquals("{\"name\" : \"robin\"}", firstJson)
        assertEquals("{\"name\" : \"robin\"}", secondJson)

        client.close()
        server.stop(0)
    }
}

object StringSerde : Serde<String> {
    override fun serialize(from: String): Result<String, SerdeException> {
        return Result.Ok(from)
    }

    override fun serialize(from: Collection<String>): Result<String, SerdeException> {
        return Result.Ok(from.joinToString())
    }

    override fun deserialize(from: String): Result<String, SerdeException> {
        return Result.Ok(from)
    }
}