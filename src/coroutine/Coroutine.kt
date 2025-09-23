package coroutine

import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.coroutines.*
import java.util.concurrent.*

class Continue<T>(private val completion: Continuation<T>): Continuation<T> {
    override val context: CoroutineContext get() = completion.context
    private var result: Result<T>? = null
    override fun resumeWith(result: Result<T>) {
        this.result = result
        println("continue - resume with result: ${result.getOrNull()}")
        completion.resumeWith(result)
    }
}

class Scope(private val coroutineContext: CoroutineContext) {
    fun launch(block: suspend() -> Unit) {
        val completion = object: Continuation<Unit> {
            override val context: CoroutineContext = coroutineContext
            override fun resumeWith(result: Result<Unit>) {
                result.onFailure { throwable -> throwable.printStackTrace() }
            }
        }
        block.startCoroutine(completion)
    }
}

class Dispatcher(private val executor: ExecutorService): CoroutineContext.Element, ContinuationInterceptor {
    override val key: CoroutineContext.Key<*> = ContinuationInterceptor
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = DispatchedContinuation(continuation, this)
    fun dispatch(block: () -> Unit) {
        executor.submit(block)
    }

    private class DispatchedContinuation<T>(
        val completion: Continuation<T>,
        val dispatcher: Dispatcher,
    ): Continuation<T> {
        override val context: CoroutineContext = completion.context
        override fun resumeWith(result: Result<T>) {
            dispatcher.dispatch {
                completion.resumeWith(result)
            }
        }
    }
}

suspend fun <T> coroutine(block: () -> T): T = suspendCoroutine { continuation ->
    println("concurrent - suspended")
    val dispatcher = continuation.context[ContinuationInterceptor] as? Dispatcher
    thread {
        try {
            val result = block()
            println("concurrent - work complete, resuming...")
            continuation.resume(result)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

fun <T> runBlocking(block: suspend () -> T): T {
    val semaphore = Semaphore(0)
    val result_ref = AtomicReference<Result<T>>()
    block.startCoroutine(object: Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) {
            result_ref.set(result)
            semaphore.release()
        }
    })
    semaphore.acquire()
    return result_ref.get().getOrThrow()
}

suspend fun delay(ms: Long) {
    suspendCoroutine<Unit> { continuation ->
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduler.schedule({
            continuation.resume(Unit)
            scheduler.shutdown()
        }, ms, TimeUnit.MILLISECONDS)
    }
}

suspend fun <T> CompletableFuture<T>.await(): T = suspendCoroutine { continuation ->
    this.whenComplete { result, exception -> 
        when(exception) {
            null -> continuation.resume(result)
            else -> continuation.resumeWithException(exception)
        }
    }
}

fun scope(executor: ExecutorService): Scope {
    val dispatcher = Dispatcher(executor)
    val context = EmptyCoroutineContext + dispatcher
    return Scope(context)
}

