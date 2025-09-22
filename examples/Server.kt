import json.*
import kotlinx.coroutines.channels.*
import server.*
import util.*

private val persons = mutableMapOf<Int, Person>()

fun main() {
    Logger.load("/log.conf")

    Server(8080).apply {
        router("/persons") {
            get {
                respond(StatusCode.OK, persons.values)
            }

            get("/:id") {
                val person = pathParam["id"]?.let { id -> persons[id.toInt()] }
                when (person) {
                    null -> respond(StatusCode.NotFound)
                    else -> respond(StatusCode.OK, person)
                }
            }

            post("/:id") {
                JsonSerde.deserialize(readBody())
                    .map(Person::from)
                    .onSuccess {
                        val id = pathParam["id"]?.toInt() ?: badRequest("param id not found")
                        persons[id] = it
                        respond(StatusCode.Created)
                    }
                    .onFailure {
                        respond(StatusCode.BadRequest, it.message)
                    }
            }
        }
        router("/admin") {
            enable(OAuth)
            get {
                respond(StatusCode.OK, Person("Robin"))
            }
        }
    }.start(0)
}

data class Person(
    val name: String
) : Into<Json> {
    override fun into(): Json = mapOf(
        Person::name.name to name
    )

    companion object {
        fun from(json: Json): Person {
            val name: String by json
            return Person(name)
        }
    }
}
