package json

import serde.*
import util.*

object JsonSerde : Serde<Json> {
    override fun serialize(from: Json): Result<String, SerdeException> {
        return try {
            Result.Ok(Serializer.serialize(from))
        } catch (e: SerdeException) {
            Result.Err(e)
        }
    }

    override fun serialize(from: Collection<Json>): Result<String, SerdeException> {
        return try {
            val objects = from.joinToString { serialize(it).unwrap() }
            Result.Ok("[ $objects ]")
        } catch (e: SerdeException) {
            Result.Err(e)
        }
    }

    override fun deserialize(from: String): Result<Json, SerdeException> {
        return JsonParser.parse(from).map {
            it.result
        }.mapError {
            SerdeException(it.reason, it.pos.toString(), null)
        }
    }
}

private object Serializer {
    fun serialize(json: Json): String = buildString {
        append("{") // todo: support "["
        append(traverse(json))
        append("}") // todo: support "]"
    }

    private fun traverse(json: Json): String =
        buildString {
            json.forEach { (key, value) ->
                append(
                    when (value) {
                        is String -> str(key, value)
                        is Number -> num(key, value)
                        is Boolean -> bool(key, value)
                        is Map<*, *> -> obj(key, value)
                        is List<*> -> arr(key, value)
                        null -> nil(key)
                        else -> serdeError("Cant serialize json", key, value)
                    }
                )
                append(", ")
            }
        }.removeSuffix(", ")

    private fun str(key: String, value: String): String {
        return "\"$key\" : \"$value\""
    }

    private fun num(key: String, value: Number): String {
        return "\"$key\" : $value"
    }

    private fun bool(key: String, value: Boolean): String {
        return "\"$key\" : $value"
    }

    @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
    private fun obj(key: String, value: Map<*, *>): String {
        val value = traverse(value as Map<String, Any?>)
        return buildString {
            append("\"$key\" : ")
            append("{ ")
            append(value)
            append(" }")
        }
    }

    @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
    private fun arr(key: String, values: List<Any?>): String {
        val values = buildString {
            values.forEach {
                append(
                    when (it) {
                        is String -> "\"$it\""
                        is Number -> "$it"
                        is Boolean -> "$it"
                        is Map<*, *> -> traverse(it as Map<String, Any?>)
                        null -> "\"null\""
                        else -> serdeError("Cant serialize array value", key, it)
                    }
                )
                append(", ")
            }
        }.removeSuffix(", ")
        return buildString {
            append("\"$key\" : ")
            append("[ ")
            append(values)
            append(" ]")
        }
    }

    private fun nil(key: String, includeNulls: Boolean = true): String {
        return when (includeNulls) {
            true -> "\"$key\" : \"null\""
            false -> ""
        }
    }

    private fun serdeError(reason: String, key: String, value: Any?): Nothing {
        throw SerdeException(reason, key, value)
    }
}

