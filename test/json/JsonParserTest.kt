package json

import test.*
import util.*

class JsonParserTest {

    @Test
    fun `can parse complex json`() {
         JsonParser.parse(json).unwrap() 
        // TODO: assert
    }

    @Test
    fun `can parse json to data class`() {
        val parsed = JsonParser.parse(json)
        val person = parsed.map { Person.from(it.result) }.unwrap()

        val expected = Person(
            name = Name(first = "Ola", last = "Normann"),
            age = 30,
            alive = true,
            kids = listOf("AB", "CD")
        )

        assertEq(expected, person)
    }

    @Test
    fun `can serialize to json`() {
        val person = Person(
            name = Name(first = "Ola", last = "Normann"),
            age = 30,
            alive = true,
            kids = listOf("AB", "CD")
        )
        val json = JsonSerde.serialize(person.into()).unwrap()
        fun String.removeWhitespaces(): String = replace("\n", "").replace(" ", "")
        assertEq(json.removeWhitespaces(), json.removeWhitespaces())
    }

    @Test
    fun `can parse a list of object`() {
        val persons = listOf(
            Person(
                name = Name(first = "Ola", last = "Normann"),
                age = 30,
                alive = true,
                kids = listOf("AB", "CD")
            ),
            Person(
                name = Name(first = "Kari", last = "Normann"),
                age = 30,
                alive = true,
                kids = listOf("AB", "CD")
            )
        )
        val jsons = persons.map { it.into() }
        JsonSerde.serialize(jsons).unwrap()
        // TODO: assert
    }

    internal data class Person(
        val name: Name,
        val age: Int,
        val alive: Boolean,
        val kids: List<String>,
    ) : Into<Json> {
        companion object {
            fun from(json: Json): Person {
                val person: Map<String, Any> by json
                val name = Name.from(person)
                val age: Int by person
                val alive: Boolean by person
                val kids: List<String> by person
                return Person(name, age, alive, kids)
            }
        }

        override fun into(): Json =
            mapOf(
                "person" to mapOf(
                    Person::name.name to name.into(),
                    Person::age.name to age,
                    Person::alive.name to alive,
                    Person::kids.name to kids
                )
            )
    }

    internal data class Name(val first: String, val last: String) {
        companion object {
            fun from(map: Json): Name {
                val name: Json by map
                val first: String by name
                val last: String by name
                return Name(first, last)
            }
        }

        fun into(): Json = mapOf(
            Name::first.name to first,
            Name::last.name to last,
        )
    }
}

private val json = """
{
    "person": {
        "name": {
            "first": "Ola",
            "last": "Normann"
        },
        "age": 30,
        "alive": true,
        "kids": [ "AB", "CD" ]
    }
}
""".trimIndent()
