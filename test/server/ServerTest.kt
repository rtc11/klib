package server

import client.*
import json.*
import kotlinx.coroutines.*
import test.*
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
        server.start(3)

        fun fetchText(): HttpResponse = runBlocking {
            Client().use {
                it.get(URI("http://localhost:8080/text")) {
                    setHeader("Content-Type", ContentType.Text.withCharsetUtf8())
                    setHeader("Accept", ContentType.Text.value)
                }
            }
        }

        fun fetchJson(): HttpResponse  = runBlocking {
            Client().use {
                it.get(URI("http://localhost:8080/json")) {
                    setHeader("Content-Type", ContentType.Json.withCharsetUtf8())
                    setHeader("Accept", ContentType.Json.value)
                }
            }
        }
        try {
            val firstText = StringSerde.deserialize(fetchText().body).expect("hello")
            val secondText = StringSerde.deserialize(fetchText().body).expect("hello")
            val firstJson = fetchJson().body
            val secondJson = fetchJson().body
            assertEq("hello", firstText)
            assertEq("hello", secondText)
            assertEq("{\"name\" : \"robin\"}", firstJson)
            assertEq("{\"name\" : \"robin\"}", secondJson)
        } finally {
            server.stop(0)
        }
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
