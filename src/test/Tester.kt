package test

import java.io.File
import java.util.logging.Level
import java.lang.reflect.Method
import kotlin.io.println
import util.*

fun main(args: Array<String>) {
    Logger.load("/log.conf")
    Logger.level = Level.OFF
    val path = args.firstOrNull() ?: "/"
    ClassLoader.findTestClasses(path)
}

@Target(AnnotationTarget.FUNCTION) 
annotation class Test


// 
//  
// 
// 
// 
// 
// 
// 
// 
data class TestResult(private val name: String, private val success: Boolean, private val time: Long) {
    private fun status(): String = when (success) {
        true -> "".green_text() + "PASS".green_bg().gray_text() + "".green_text()
        false -> "".red_text() + "FAIL".red_bg().gray_text() + "".red_text()
    }

    private fun time(): String = "${time/1_000_000} ms".padEnd(9, ' ').yellow_text()

    private fun name(): String = "$name ".padEnd(40, '˒').white_text()


    override fun toString(): String = " ${time()} ${name()} ${status()}"
}


object ClassLoader {

    // fun runTest(className: String, testName: String): Result<TestResult, Throwable> {
    //     val testClass = Class.forName(className)
    //     return testClass.methods
    //         .filter { it.isTest() }
    //         .filter { it.name == testName }
    //         .singleOrNull()?.let { test -> testClass.runTest(test) }
    //         ?: Result.Err(IllegalArgumentException("Test $testName not found in class $className"))
    // }

    // fun runTest(clazz: Class<*>, method: Method): Result<TestResult, Throwable> {
    fun runTest(className: String, testName: String): Result<TestResult, Throwable> {
        val testClass = Class.forName(className)
        return testClass.methods
            .filter { it.isTest() }
            .filter { it.name == testName }
            .singleOrNull()?.let { test -> testClass.runTest(test) }
            ?: Result.Err(IllegalArgumentException("Test $testName not found in class $className"))
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
                // println("classname".light_gray_text())
                //
                // val line = "".gray_text() + " $classname ".light_gray_text() + "".gray_text()
                // println("          ".gray_bg() + line + "          ".gray_bg())
                println("".gray_text() + classname.gray_bg().light_gray_text() + "".gray_text())

                val c = clazz.getDeclaredConstructor().newInstance()
                val res = c::class.java.methods
                    .filter { it.isTest() }
                    .map { test -> c.runTest(test) }

                res.filterOk().forEach { result -> println(result) }
                res.filterErr().forEach { err -> println(err) }
            }
    }
}
// color_blue = '#458588'
private fun String.blue_text(): String = "${ANSI24bit.fg(69, 133, 136)}$this${ANSI24bit.reset()}"
private fun String.blue_bg(): String = "${ANSI24bit.bg(69, 133, 136)}$this${ANSI24bit.reset()}"
// color_green = '#98971a'
private fun String.yellow_text(): String = "${ANSI24bit.fg(152, 151, 26)}$this${ANSI24bit.reset()}"
private fun String.yellow_bg(): String = "${ANSI24bit.bg(152, 151, 26)}$this${ANSI24bit.reset()}"
// color_purple = '#b16286'
private fun String.purple_text(): String = "${ANSI24bit.fg(177, 98, 134)}$this${ANSI24bit.reset()}"
private fun String.purple_bg(): String = "${ANSI24bit.bg(177, 98, 134)}$this${ANSI24bit.reset()}"

private fun String.white_text(): String = "${ANSI24bit.fg(222, 222, 222)}$this${ANSI24bit.reset()}"
private fun String.green_bg(): String = "${ANSI24bit.bg(0, 222, 155)}$this${ANSI24bit.reset()}"
private fun String.green_text(): String = "${ANSI24bit.fg(0, 222, 155)}$this${ANSI24bit.reset()}"
private fun String.dark_yellow_text(): String = "${ANSI24bit.fg(100, 100, 0)}$this${ANSI24bit.reset()}"
private fun String.red_bg(): String = "${ANSI24bit.bg(250, 100, 100)}$this${ANSI24bit.reset()}"
private fun String.red_text(): String = "${ANSI24bit.fg(250, 100, 100)}$this${ANSI24bit.reset()}"
private fun String.gray_text(): String = "${ANSI24bit.fg(60, 60, 60)}$this${ANSI24bit.reset()}"
private fun String.gray_bg(): String = "${ANSI24bit.bg(60, 60, 60)}$this${ANSI24bit.reset()}"
private fun String.light_gray_text(): String = "${ANSI24bit.fg(135, 125, 125)}$this${ANSI24bit.reset()}"
private fun Long.stop(): Long = (System.nanoTime() - this)
private fun Method.isTest(): Boolean = isAnnotationPresent(Test::class.java)
private fun Class<*>.hasTests(): Boolean = methods.any{ it.isTest() }

