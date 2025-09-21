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

    val client = Client()
    fun getPerson(id: Int): Person {
        return client.getPerson(id).let { res ->
            log.info("find person $id")
            when (res.statusCode) {
                404 -> {
                    log.info("person missing. creating person..")
                    client.createPerson(id, Person("Alice")).let { log.info("person created") } 
                    getPerson(id)
                }
                200 -> Person.from(JsonSerde.deserialize(res.body).unwrap()) 
                else -> error("unexpected status code: ${res.statusCode}")
            }
        }
    }
    
    log.info("person: ${getPerson(1)}")
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

