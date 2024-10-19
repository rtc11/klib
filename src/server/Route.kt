package server

import server.RequestMethod.*
import util.*
import kotlin.io.path.*

class Route(
    val method: RequestMethod,
    val path: Path,
    val handler: suspend Exchange.() -> Any?,
) {
    /**
     * Run whats configured for the given route (e.g. route{get{ ... }}
     */
    suspend fun handle(exchange: Exchange, extensions: List<Extension>): Any? {
        extensions.forEach { it.activate() }
        return handler(exchange)
    }
}

class Router(private val prefix: String) {
    private val routes: MutableList<Route> = mutableListOf()
    private val extensions: MutableList<Extension> = mutableListOf()

    internal fun lookupRoute(exchange: Exchange): Route? = routes
        .filter { route -> exchange.method == route.method || exchange.method == HEAD && route.method == GET }
        .singleOrNull { route ->
            when (val match = route.path.match(exchange.path.removePrefix(prefix))) {
                is Result.Err -> false
                is Result.Ok -> {
                    exchange.pathParam = PathParams(match.value.groups)
                    true
                }
            }
        }

    fun enable(ext: Extension) = extensions.add(ext)
    fun extensions() = extensions

    fun get(path: String = "", handler: suspend Exchange.() -> Any?) = add(Route(GET, Path(path), handler))
    fun post(path: String = "", handler: Exchange.() -> Any?) = add(Route(POST, Path(path), handler))
    fun put(path: String = "", handler: Exchange.() -> Any?) = add(Route(PUT, Path(path), handler))
    fun patch(path: String = "", handler: Exchange.() -> Any?) = add(Route(PATCH, Path(path), handler))
    fun delete(path: String = "", handler: Exchange.() -> Any?) = add(Route(DELETE, Path(path), handler))
    fun options(path: String = "", handler: Exchange.() -> Any?) = add(Route(OPTIONS, Path(path), handler))
    fun head(path: String = "", handler: Exchange.() -> Any?) = add(Route(HEAD, Path(path), handler))

    private fun add(route: Route) {
        routes.add(route)
        log.debug("route ${route.method} $prefix${route.path.path}")
    }
}

enum class RequestMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    HEAD
}

class Path(val path: String) {
    private val paramRegex = Regex("(^|/):([^/]+)")
    private val paramReplacement = "$1(?<$2>[^/]+)"

    fun match(other: String): Result<MatchResult, Any?> =
        paramRegex.replace(path, paramReplacement).toRegex().matchEntire(other)
            ?.let { Result.Ok(it) }
            ?: Result.Err(null)
}

class PathParams(private val groups: MatchGroupCollection) : Map<String, String?> {
    override val entries get() = error("entries was called")
    override val keys get() = error("keys was called")
    override val values get() = groups.map { it?.value }
    override val size get() = groups.size
    override fun isEmpty() = groups.isEmpty()
    override fun get(key: String) = runCatching { groups.get(key) }.getOrNull()?.value
    override fun containsKey(key: String) = get(key) != null
    override fun containsValue(value: String?) = groups.find { it != null && it.value == value } != null
}

sealed class ContentType(val value: String) {
    data object Html : ContentType("text/html")
    data object Text : ContentType("text/plain")
    data object Xml : ContentType("text/xml")
    data object EventStream : ContentType("text/event-stream")
    data object Json : ContentType("application/json")
    data object Csv : ContentType("application/csv")
    data object Pdf : ContentType("application/pdf")
    data object WwwForm : ContentType("application/x-www-form-urlencoded")
    data object FormData : ContentType("multipart/form-data")
    data object Unknown : ContentType("application/octet-stream")

    fun isText(): Boolean = this in listOf(Html, Text, Xml, EventStream)

    fun withCharsetUtf8(): String = when (isText()) {
        true -> "$value; charset=${Charsets.UTF_8}"
        false -> value
    }
}

object MimeType {
    fun forType(file: java.nio.file.Path): String? = fileExtensionsToMime[file.extension]

    fun isText(contentType: String): Boolean = contentType.contains("json", true)
            || contentType.contains("xml", true)
            || contentType.contains("csv", true)
            || contentType == ContentType.WwwForm.value
            || contentType.startsWith("text/", true)

    fun withCharset(contentType: String): String = when (isText(contentType)) {
        true -> "$contentType; charset=${Charsets.UTF_8}"
        false -> contentType
    }

    private val fileExtensionsToMime = mapOf(
        "html" to ContentType.Html.value,
        "txt" to ContentType.Text.value,
        "xml" to ContentType.Xml.value,
        "xsd" to ContentType.Xml.value,
        "csv" to ContentType.Csv.value,
        "json" to ContentType.Json.value,
        "pdf" to ContentType.Pdf.value,
        "asice" to "application/vnd.etsi.asic-e+zip",
        "css" to "text/css",
        "csv" to "text/csv",
        "gif" to "image/gif",
        "gz" to "application/gzip",
        "ico" to "image/vnd.microsoft.icon",
        "ics" to "text/calendar",
        "jar" to "application/java-archive",
        "jpeg" to "image/jpeg",
        "jpg" to "image/jpeg",
        "js" to "text/javascript",
        "mjs" to "text/javascript",
        "mp3" to "audio/mpeg",
        "mp4" to "video/mp4",
        "oga" to "audio/oga",
        "ogv" to "video/ogv",
        "otf" to "font/otf",
        "png" to "image/png",
        "svg" to "image/svg+xml",
        "ttf" to "font/ttf",
        "webp" to "image/webp",
        "woff" to "font/woff",
        "woff2" to "font/woff2",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "zip" to "application/zip",
    )
}

@JvmInline
value class StatusCode(val code: Int) {
    companion object {
        val OK = StatusCode(200)
        val Created = StatusCode(201)
        val Accepted = StatusCode(202)
        val NoContent = StatusCode(204)
        val BadRequest = StatusCode(400)
        val Unauthorized = StatusCode(401)
        val Forbidden = StatusCode(403)
        val NotFound = StatusCode(404)
        val MethodNotAllowed = StatusCode(405)
        val NotAcceptable = StatusCode(406)
        val Conflict = StatusCode(409)
        val Gone = StatusCode(410)
        val UnsupportedMediaType = StatusCode(415)
        val UnprocessableEntity = StatusCode(422)
        val Locked = StatusCode(423)
        val TooEarly = StatusCode(425)
        val TooManyRequests = StatusCode(429)
        val InternalServerError = StatusCode(500)
        val NotImplemented = StatusCode(501)
        val BadGateway = StatusCode(502)
        val ServiceUnavailable = StatusCode(503)
        val GatewayTimeout = StatusCode(504)
    }
}
