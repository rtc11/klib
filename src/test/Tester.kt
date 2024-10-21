package test

import java.io.File
import java.util.logging.Level
import java.lang.reflect.Method
import kotlin.io.println
import util.*

data class Cmd(val className: String, val testName: String)

object Cli { 
    fun parse(args: Array<String>): Cmd {
        var clazzName = ""
        var testName = ""
        args.forEachIndexed { i, arg -> 
            if (i == 0) {
                clazzName = arg
            } else {
                testName += arg + " "
            }
        }

        return Cmd(clazzName, testName.trim())
    }
} 

fun main(args: Array<String>) {
    Logger.load("/log.conf")
    Logger.level = Level.OFF

    if (args.isEmpty()) {
        ClassLoader.findTestClasses("/")
    } else {
        val cmd = Cli.parse(args)
        println("Running test ${cmd.testName} in class ${cmd.className}")
        ClassLoader.runTest(cmd.className, cmd.testName)
    }
}

@Target(AnnotationTarget.FUNCTION) 
annotation class Test

//         
data class TestResult(private val name: String, private val success: Boolean, private val time: Long) {
    private fun status(): String = when (success) {
        true -> "".text(Color.GREEN) + "PASS".bg(Color.GREEN).text(Color.GRAY) + "".text(Color.GREEN)
        false -> "".text(Color.RED) + "FAIL".bg(Color.RED).text(Color.GRAY) + "".text(Color.RED)
    }

    private fun time(): String = "${time/1_000_000} ms".padEnd(9, ' ').text(Color.YELLOW)
    private fun name(): String = "$name ".padEnd(40, '˒').text(Color.WHITE)

    override fun toString(): String = " ${time()} ${name()} ${status()}"
}

object ClassLoader {

    fun runTest(className: String, testName: String) {
        val testClass = Class.forName(className)
        val clazz = testClass .getDeclaredConstructor().newInstance()
        val res = clazz::class.java.methods
            .filter { it.isTest() }
            .filter { it.name == testName }
            .singleOrNull()?.let { test -> clazz.runTest(test) }
            ?: Result.Err(IllegalArgumentException("Test $testName not found in class $className"))

        when(res) {
            is Result.Ok -> println(res.value)
            is Result.Err -> println(res.error)
        } 
    }

    private fun Any.runTest(method: Method): Result<TestResult, Throwable> {
        val start = System.nanoTime()
        return try {
            method.invoke(this)
            Result.Ok(TestResult(method.name, true, start.stop()))
        } catch (e: AssertionError) {
            Result.Ok(TestResult(method.name, false, start.stop()))
        } catch (e: Exception) {
            when (e.cause) {
                is AssertionError -> Result.Ok(TestResult(method.name, false, start.stop()))
                else -> Result.Err(e)
            }
        }
    }

    fun findTestClasses(path: String = "/") {
        val url = Resource.url(path)
        val dir = File(url.file)
        dir.walk()
            .filter { it.extension == "class" }
            .filterNot { it.name.contains('$') }
            .map { 
                val qn = it.canonicalPath.removePrefix(dir.canonicalPath).removePrefix("/").replace("/", ".").removeSuffix(".class")
                qn to Class.forName(qn) 
            }
            .filter { (_, clazz) -> clazz.hasTests() }
            .forEach { (classname, clazz) ->
                println("".text(Color.GRAY) + classname.bg(Color.GRAY).text(Color.LIGHT_GRAY) + "".text(Color.GRAY))

                val c = clazz.getDeclaredConstructor().newInstance()
                val res = c::class.java.methods
                    .filter { it.isTest() }
                    .map { test -> c.runTest(test) }

                res.filterOk().forEach { result -> println(result) }
                res.filterErr().forEach { err -> println(err) }
            }
    }
}

data class Color(val r: Int, val b: Int, val g: Int) {
    companion object {
        val BLUE = Color(69, 133, 136)
        val YELLOW = Color(152, 151, 26)
        val PURPLE = Color(177, 98, 134)
        val WHITE = Color(222, 222, 222)
        val GREEN = Color(0, 222, 155)
        val DARK_YELLOW = Color(100, 100, 0)
        val RED = Color(250, 100, 100)
        val GRAY = Color(60, 60, 60)
        val LIGHT_GRAY = Color(135, 125, 125)
    }
}

private fun String.text(color: Color): String = "${ANSI24bit.fg(color.r, color.b, color.g)}$this${ANSI24bit.reset()}"
private fun String.bg(color: Color): String = "${ANSI24bit.bg(color.r, color.b, color.g)}$this${ANSI24bit.reset()}"
private fun Long.stop(): Long = (System.nanoTime() - this)
private fun Method.isTest(): Boolean = isAnnotationPresent(Test::class.java)
private fun Class<*>.hasTests(): Boolean = methods.any{ it.isTest() }

