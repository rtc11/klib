package serde

import util.Result

//class SerdeException(msg: String) : RuntimeException(msg)
class SerdeException(val reason: String, val key: String, val value: Any?) : RuntimeException()

interface Serde<T> {
    fun serialize(from: T): Result<String, SerdeException>
    fun serialize(from: Collection<T>): Result<String, SerdeException>
    fun deserialize(from: String): Result<T, SerdeException>
}
