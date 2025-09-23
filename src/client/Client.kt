package client

import coroutine.*
import serde.*
import util.*
import java.net.*
import java.net.http.*
import java.net.http.HttpClient
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

typealias Request = HttpRequest.Builder.() -> HttpRequest.Builder

interface HttpClient {
    suspend fun get(url: URI, modifier: Request): HttpResponse
}

class Client : client.HttpClient, AutoCloseable {
    private val http: HttpClient = HttpFactory.new()
    private val log = logger("client")

    override suspend fun get(url: URI, modifier: Request): HttpResponse {
        val req = HttpRequest.newBuilder()
            .modifier()
            .uri(url)
            .timeout(10.seconds.toJavaDuration())
            .build()

        val start = System.nanoTime()
        val res = http.sendAsync(req, java.net.http.HttpResponse.BodyHandlers.ofString()).await()
        val timedMs = (System.nanoTime() - start) / 1_000_000
        val body = res.body().trim()
        res.headers()

        log.trace("${req.method()} $url ${res.statusCode()} [${timedMs}ms]")

        return HttpResponse(res.statusCode(), res.headers().map(), body)
    }

    suspend fun post(url: URI, body: String, modifier: Request): HttpResponse {
        val req = HttpRequest.newBuilder()
            .modifier()
            .uri(url)
            .timeout(10.seconds.toJavaDuration())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val start = System.nanoTime()
        val res = http.sendAsync(req, java.net.http.HttpResponse.BodyHandlers.ofString()).await()
        val timedMs = (System.nanoTime() - start) / 1_000_000
        res.headers()

        log.trace("${req.method()} $url ${res.statusCode()} [${timedMs}ms]")

        return HttpResponse(res.statusCode(), res.headers().map(), "")
    }

    override fun close() = http.close()
}


data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: String
)

private object HttpFactory {
    fun new(): HttpClient = HttpClient.newBuilder().connectTimeout(5.seconds.toJavaDuration()).build()
}
