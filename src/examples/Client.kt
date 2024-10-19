package examples

import client.*
import java.net.*
import json.*
import kotlinx.coroutines.*
import server.*
import util.*

fun main() {
    Logger.load("/log.conf")
    val log = logger("example")

    Client().use { client ->
        client.getPerson(1).let { res ->
            when (res.statusCode) {
                404 -> log.warn("person not found")
                200 -> log.info(Person.from(JsonSerde.deserialize(res.body).unwrap()))
                else -> log.error("unexpected status code: ${res.statusCode}")
            }
        }
    }

    Client().use { client -> client.createPerson(1, Person("Alice")) }

    Client().use { client ->
        client.getPerson(1).let { res ->
            when (res.statusCode) {
                404 -> log.warn("person not found")
                200 -> log.info(Person.from(JsonSerde.deserialize(res.body).unwrap()))
                else -> log.error("unexpected status code: ${res.statusCode}")
            }
        }
    }
}

private fun Client.getPerson(id: Int): HttpResponse = runBlocking {
    get(URI("http://localhost:8080/persons/$id")) {
        setHeader("Content-Type", ContentType.Json.withCharsetUtf8())
        setHeader("Accept", ContentType.Json.value)
    }
}

private fun Client.createPerson(id: Int, person: Person): HttpResponse = runBlocking {
    val json = JsonSerde.serialize(person.into()).unwrap()
    post(URI("http://localhost:8080/persons/$id"), json) {
        setHeader("Content-Type", ContentType.Json.withCharsetUtf8())
    }
}

