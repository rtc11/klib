package server

import com.sun.net.httpserver.*
import json.*
import util.*
import java.io.*

class Exchange(private val exchange: HttpExchange) : AutoCloseable {
    internal val method: RequestMethod get() = RequestMethod.valueOf(exchange.requestMethod)
    internal val path: String get() = exchange.requestURI.path
    var pathParam = mapOf<String, String?>()

    suspend fun handle(route: Route?, extensions: List<Extension>) {
        try {
            if (route == null) notFound("Route not found")
            route.handle(this, extensions)
        } catch (err: StatusError) {
            respond(err.code, err.content)
        }
    }

    fun <T : Into<Json>> respond(status: StatusCode, body: T) {
        JsonSerde.serialize(body.into()).onSuccess { json ->
            send(status, json.encodeToByteArray(), ContentType.Json)
        }
    }

    fun <T : Collection<Into<Json>>> respond(status: StatusCode, body: T) {
        JsonSerde.serialize(body.map { it.into() }).onSuccess { json ->
            send(status, json.encodeToByteArray(), ContentType.Json)
        }
    }

    fun respond(status: StatusCode, message: String? = null) {
        when (message) {
            null -> send(status, null, null)
            else -> send(status, message.encodeToByteArray(), ContentType.Text)
        }
    }

    fun inputStream(): InputStream =
        when (method) {
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.PATCH,
            RequestMethod.OPTIONS -> exchange.requestBody

            else -> error("$method should not have body")
        }

    fun readBody(): String = inputStream().bufferedReader().use { it.readText() }

    fun send(event: Event) {
        startEventStream()
        event.sendTo(exchange.responseBody)
    }

    private fun beginResponse(
        status: StatusCode, contentLength: Long? = null, contentType: ContentType? = null
    ): Result<OutputStream, String> {
        val contentLength = contentLength ?: 0
        contentType?.let { exchange.responseHeaders["Content-Type"] = contentType.value }
        val isBodyNotAllowed = method in listOf(RequestMethod.HEAD, RequestMethod.OPTIONS)
        when {
            contentLength == 0L -> exchange.sendResponseHeaders(status.code, -1)
            isBodyNotAllowed -> exchange.sendResponseHeaders(status.code, -1)
            else -> exchange.sendResponseHeaders(status.code, contentLength)
        }

        return when (isBodyNotAllowed) {
            true -> Result.Err("body not allowed in HTTP $method")
            false -> Result.Ok(exchange.responseBody)
        }
    }

    private fun send(status: StatusCode, body: ByteArray? = null, contentType: ContentType? = null) {
        when (val out = beginResponse(status, body?.size?.toLong(), contentType)) {
            is Result.Ok -> body?.let { out.value.write(it) }
            is Result.Err -> internalServerError(out.error)
        }
    }

    fun startEventStream() = beginResponse(StatusCode.OK, null, ContentType.EventStream)

    override fun close() = exchange.close()
}
