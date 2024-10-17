package client

import kotlinx.coroutines.future.*
import serde.*
import util.*
import java.net.*
import java.net.http.*
import java.net.http.HttpClient
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

typealias Request = HttpRequest.Builder.() -> HttpRequest.Builder

class HttpClientException(msg: String, cause: Throwable) : RuntimeException(msg, cause)

interface HttpClient<T> {
    suspend fun get(url: URI, modifier: Request): Result<T, HttpClientException>
}

class Client<T>(
    private val serde: Serde<T>,
//    private val host: String? = null,
//    val parser: (String) -> ParserResult<T>,
//    val retries: Int = 1,
//    val backoff: Duration = 1.seconds,
) : client.HttpClient<T>, AutoCloseable {
    private val http: HttpClient = HttpFactory.new()
    private val log = logger("client")

    override suspend fun get(url: URI, modifier: Request): Result<T, HttpClientException> {
        val req = HttpRequest.newBuilder()
            .modifier()
            .uri(url)
            .timeout(10.seconds.toJavaDuration())
            .build()

        val start = System.nanoTime()
        val res = http.sendAsync(req, HttpResponse.BodyHandlers.ofString()).await()
        val timedMs = (System.nanoTime() - start) / 1_000_000
        val body = res.body().trim()
        res.headers()

        log.info("${req.method()} $url ${res.statusCode()} [${timedMs}ms]")

        return serde.deserialize(body).mapError {
            HttpClientException("Failed to deserialize response", it)
        }
    }

    override fun close() = http.close()
}

private object HttpFactory {
    fun new(): HttpClient = HttpClient.newBuilder().connectTimeout(5.seconds.toJavaDuration()).build()
}
