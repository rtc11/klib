package coroutine

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.*
import test.*

class CoroutinesTest {

    @Test
    fun `runBlocking`() = runBlocking {
        val result = "Hello, World!"
        eq(result, "Hello, World!")
    }

    @Test
    fun `delay`() = runBlocking {
        val start = System.currentTimeMillis()
        val delayMillis = 10L
        delay(delayMillis)
        val end = System.currentTimeMillis()
        val elapsed = end - start
        assert(elapsed >= delayMillis && elapsed < delayMillis + 5)
    }

    @Test
    fun `coroutine offloading`() = runBlocking {
        val executor = Executors.newFixedThreadPool(1)
        try {
            runBlocking {
                val mainThreadName = Thread.currentThread().name
                val workerThreadName = AtomicReference<String>()
                val scope = Scope(EmptyCoroutineContext + Dispatcher(executor))
                scope.launch {
                    workerThreadName.set(Thread.currentThread().name)
                    assert(workerThreadName.get() != mainThreadName)
                }
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(3, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun `exception handling`() {
        try {
            runBlocking {
                error("Test exception")
            }
            fail("Expected an IllegalStateException")
        } catch (e: IllegalStateException) {
            eq("Test exception", e.message)
        }
    }
}

