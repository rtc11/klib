package server

import com.sun.net.httpserver.*
import coroutine.*
import server.StatusError.*
import util.*
import java.lang.Runtime.getRuntime
import java.net.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.concurrent.*
import kotlin.coroutines.*

val log = logger("server")

class Server(
    port: Int = 8080,
    threadPoolSize: Int = getRuntime().availableProcessors(),
) {
    private val listener = InetSocketAddress(port)
    private val worker: ExecutorService = Executors.newWorkStealingPool(threadPoolSize)
    private val http: HttpServer = HttpServer.create()
    private val requestScope = scope(worker)
    private val onStartHandlers = mutableListOf<Runnable>()
    private val onStopHandlers = mutableListOf<Runnable>()

    /**
     * Start the http server
     */
    fun start(gracefulShutdownSec: Int = 3) {
        http.bind(listener, 0)
        log.info("server is on http://${listener.hostString}:${listener.port}")
        http.start()
        getRuntime().addShutdownHook(thread(start = false) { stop(gracefulShutdownSec) })
        onStartHandlers.forEach { it.run() }
    }

    /**
     * Stop the http server
     */
    fun stop(delaySec: Int) {
        log.info("server is stopping...")
        http.stop(delaySec)
        onStopHandlers.reversed().forEach { it.run() }
    }

    /**
     * Do something when server har started
     */
    fun onStart(handle: Runnable) = onStartHandlers.add(handle)

    /**
     * Do something after server has stopped
     */
    fun onStop(handler: Runnable) = onStopHandlers.add(handler)

    /**
     * Register a new route on the server
     */
    fun router(prefix: String, block: Router.() -> Unit = {}) {
        val router = Router(prefix).apply(block)
        http.createContext(prefix) { exchange ->
            // requestScope.launch(ThreadNameContext() /* + Dispatchers.IO */) {
            requestScope.launch {
                val exchange = Exchange(exchange)
                val route = router.lookupRoute(exchange)
                val extensions = router.extensions()
                exchange.handle(route, extensions)
                exchange.close()
            }
        }
    }
}

// internal class ThreadNameContext(
//     private val requestId: String = RequestId.next(),
// ) : ThreadContextElement<String?>, AbstractCoroutineContextElement(Key) {
//     companion object Key : CoroutineContext.Key<ThreadNameContext>
//
//     override fun updateThreadContext(context: CoroutineContext): String {
//         return Thread.currentThread().also { it.name = requestId }.name
//     }
//
//     override fun restoreThreadContext(context: CoroutineContext, oldState: String?) {
//         Thread.currentThread().name = oldState
//     }
// }

private object RequestId {
    private val PREFIX = (0xFFFF * Math.random()).toInt().toString(16)
    private val counter = AtomicLong()
    fun next(): String = "$PREFIX-${counter.incrementAndGet()}"
}

open class NoStackTraceError(
    msg: String? = null,
    cause: Throwable? = null,
) : Exception(msg, cause) {
    override fun fillInStackTrace(): Throwable = this
}

sealed class StatusError(
    val code: StatusCode,
    val content: String?,
    cause: Throwable?,
) : NoStackTraceError(content, cause) {
    class BadRequest(content: String?, err: Throwable?) : StatusError(StatusCode.BadRequest, content, err)
    class NotFound(content: String?, err: Throwable?) : StatusError(StatusCode.NotFound, content, err)
    class NotAcceptable(content: String?, err: Throwable?) : StatusError(StatusCode.NotAcceptable, content, err)
    class Conflict(content: String?, err: Throwable?) : StatusError(StatusCode.Conflict, content, err)
    class Forbidden(content: String?, err: Throwable?) : StatusError(StatusCode.Forbidden, content, err)
    class Unauthorized(content: String?, err: Throwable?) : StatusError(StatusCode.Unauthorized, content, err)
    class Unavailable(content: String?, err: Throwable?) : StatusError(StatusCode.ServiceUnavailable, content, err)
    class Unprocessable(content: String?, err: Throwable?) : StatusError(StatusCode.UnprocessableEntity, content, err)
    class InternalServerError(content: String?, err: Throwable?) : StatusError(StatusCode.InternalServerError, content, err)

}

fun badRequest(content: String, cause: Throwable? = null): Nothing = throw BadRequest(content, cause)
fun notFound(content: String, cause: Throwable? = null): Nothing = throw NotFound(content, cause)
fun notAcceptable(content: String, cause: Throwable? = null): Nothing = throw NotAcceptable(content, cause)
fun conflict(content: String, cause: Throwable? = null): Nothing = throw Conflict(content, cause)
fun forbidden(content: String, cause: Throwable? = null): Nothing = throw Forbidden(content, cause)
fun unauthorized(content: String, cause: Throwable? = null): Nothing = throw Forbidden(content, cause)
fun unavailable(content: String, cause: Throwable? = null): Nothing = throw Unavailable(content, cause)
fun unprocessable(content: String, cause: Throwable? = null): Nothing = throw Unprocessable(content, cause)
fun internalServerError(content: String, cause: Throwable? = null): Nothing = throw InternalServerError(content, cause)
