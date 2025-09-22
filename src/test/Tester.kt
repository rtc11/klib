package test

import java.io.File
import java.util.logging.Level
import java.lang.reflect.Method
import kotlin.io.println
import util.*
import java.lang.reflect.InvocationTargetException

fun main(args: Array<String>) {
    Logger.load("/log.conf")
    Logger.level = Level.OFF

    val cmds = Commands(args).parse()

    if (cmds.isEmpty()) {
        ClassLoader.runAllTests(File("out"))
    }

    if (cmds.hasFlag("-h")) {
        println("Usage: test.sh [options]")
        println("Options:")
        println("  -h  Show this help message")
        println("  -f  Run all tests in absolute file path")
        println("  -p  Run all tests for package")
        println("  -c  Run all tests in a given class")
        println("  -t  Run a single test in a given class")
        return
    } 

    cmds.getFlagValue("-f")?.let { path ->
        val test_dir = File(path)
        if (!test_dir.exists() || !test_dir.isDirectory) {
            println("[ERR] Path '$path' not found or is not a directory.")
            System.exit(1)
        }
        ClassLoader.runAllTests(test_dir)
        // val className = path.split("/")
        //     .dropWhile { it != "test" }.drop(1)
        //     .joinToString(".")
        //     .removeSuffix(".kt")
        //
        //     if(!ClassLoader.runTests(className)) {
        //         System.exit(1)
        //     }
    }

    cmds.getFlagValue("-p")?.let { path ->
        val pkg_dir = File("out/${path.replace(".", File.separator)}")
        if (!pkg_dir.exists()) {
            println("[ERR] Package directory '${pkg_dir.absolutePath}' not found.")
            System.exit(1)
        }
        ClassLoader.runAllTests(pkg_dir, File("out"))
    }

    cmds.getFlagValue("-c")?.let { class_name ->
        val test_name = cmds.getFlagValue("-t")
        if (test_name != null) {
            if (!ClassLoader.runTest(class_name, test_name)) System.exit(1)
        } else {
            if (!ClassLoader.runTests(class_name)) System.exit(1)
        }
    }
}

@Target(AnnotationTarget.FUNCTION) annotation class Test
@Target(AnnotationTarget.FUNCTION) annotation class Before
@Target(AnnotationTarget.FUNCTION) annotation class After
@Target(AnnotationTarget.FUNCTION) annotation class Between

//         
data class TestResult(
    private val name: String,
    private val success: Boolean,
    private val time: Long,
    private val msg: String? = null,
    private val loc: String? = null,
) {
    private fun status(): String = when (success) {
        true -> "".text(Color.GREEN) + "PASS".bg(Color.GREEN).text(Color.GRAY) + "".text(Color.GREEN)
        false -> "".text(Color.RED) + "FAIL".bg(Color.RED).text(Color.GRAY) + "".text(Color.RED)
    }

    private fun time(): String = "${time/1_000_000} ms".padEnd(9, ' ').text(Color.DARK_YELLOW)
    private fun name(): String = "$name ".text(Color.YELLOW)

    override fun toString(): String {
        return buildString {
            append(" ${time()} ${status()} ${name()}")
            if (!success) {
                if (msg != null) append("\n".padEnd(12, ' ') + msg.text(Color.LIGHT_GRAY))
                if (loc != null) append("\n".padEnd(12, ' ') + loc.text(Color.LIGHT_GRAY))
            }
        }
    }
}

object ClassLoader {

    fun runTest(className: String, testName: String): Boolean {
        val testClass = Class.forName(className)
        val clazz = testClass .getDeclaredConstructor().newInstance()
        val befores = clazz::class.java.methods.filter { it.is_before() }
        val afters = clazz::class.java.methods.filter { it.is_after() }
        befores.forEach { it(clazz) }
        val res = clazz::class.java.methods
            .filter { it.isTest() }
            .filter { it.name == testName }
            .singleOrNull()?.let { test -> clazz.invoke_test(test) }
            ?: Result.Err(IllegalArgumentException("Test $testName not found in class $className"))
        afters.forEach { it(clazz) }
        return when(res) {
            is Result.Ok -> true.also { println(res.value) }
            is Result.Err -> false.also { println(res.error) }
        } 
    }

    fun runTests(className: String): Boolean {
        val testClass = Class.forName(className)
        val clazz = testClass .getDeclaredConstructor().newInstance()
        val befores = clazz::class.java.methods.filter { it.is_before() }
        val betweens = clazz::class.java.methods.filter { it.is_between() }
        val afters = clazz::class.java.methods.filter { it.is_after() }
        val methods = clazz::class.java.methods.filter { it.isTest() }
        val results = mutableListOf<Result<TestResult, Throwable>>()
        befores.forEach { it(clazz) }
        methods.forEachIndexed { idx, test ->
            results.add(clazz.invoke_test(test))
            if (idx < methods.size - 1) betweens.forEach { it(clazz) }
        }
        afters.forEach { it(clazz) }
        results.filterOk().forEach { result -> println(result) }
        results.filterErr().forEach { err -> println(err) }
        return results.filterErr().isEmpty()
    }

    fun runAllTests(dir: File, root_dir: File = dir) {
        dir.walk()
            .filter { it.extension == "class" }
            .filterNot { it.name.contains('$') }
            .mapNotNull { file ->
                val relative_path = file.toRelativeString(root_dir)
                val class_name = relative_path 
                    // .removePrefix("test" + File.separator)
                    .replace(File.separator, ".")
                    .removeSuffix(".class")
                    // val class_name = file.canonicalPath
                    //     .removePrefix(root_dir.canonicalPath)
                    //     .removePrefix(File.separator)
                    //     .removePrefix(root_dir.canonicalPath)
                    //     .removePrefix(File.separator)
                    //     .replace(File.separator, ".")
                    //     .removeSuffix(".class")
                try {
                    when (class_name.isBlank()) {
                        true -> null
                        false -> class_name to Class.forName(class_name)
                    }
                } catch (e: ClassNotFoundException) {
                    println("[WARN] Class $class_name not found for file ${file.absolutePath}")
                    null
                } catch (e: Exception) {
                    println("[ERR] Failed to process file ${file.absolutePath}: ${e.message}")
                    null
                }
            }
            .filter { (_, clazz) -> clazz.hasTests() }
            .forEach { (classname, clazz) ->
                println("".text(Color.DARK_YELLOW) + classname.bg(Color.DARK_YELLOW).text(Color.WHITE).padEnd(9, ' ') + "".text(Color.DARK_YELLOW))
                runTests(classname)
            }
    }

    private fun Any.invoke_test(method: Method): Result<TestResult, Throwable> {
        val start = System.nanoTime()
        return try {
            method.invoke(this)
            Result.Ok(TestResult(method.name, true, start.stop()))
        } catch (e: Throwable) {
            val cause = if (e is InvocationTargetException) e.cause else e
            if (cause is AssertionError) {
                val trace = cause.stackTrace.firstOrNull { !it.className.startsWith("test.AssertionsKt") && !it.className.startsWith("test.ClassLoader") }
                val loc = trace?.let { "${it.fileName}:${it.lineNumber}" }
                Result.Ok(TestResult(method.name, false, start.stop(), cause.message, loc))
            } else {
                Result.Err(e)
            }
        }
    }
}

data class Color(val r: Int, val b: Int, val g: Int) {
    companion object {
        val BLUE = Color(69, 133, 136)        // #458588
        val YELLOW = Color(152, 151, 26)      // #98971a
        val PURPLE = Color(177, 98, 134)      // #b16286
        val WHITE = Color(222, 222, 222)      // #dedede
        val GREEN = Color(95, 226, 108)       // #5fe26c
        val DARK_YELLOW = Color(100, 100, 0)  // #646400
        val RED = Color(250, 100, 100)        // #fa6464
        val GRAY = Color(60, 60, 60)          // #3c3c3c
        val LIGHT_GRAY = Color(135, 125, 125) // #877d7d
    }
}

private fun String.text(color: Color): String = "${ANSI24bit.fg(color.r, color.b, color.g)}$this${ANSI24bit.reset()}"
private fun String.bg(color: Color): String = "${ANSI24bit.bg(color.r, color.b, color.g)}$this${ANSI24bit.reset()}"
private fun Long.stop(): Long = (System.nanoTime() - this)
private fun Method.isTest(): Boolean = isAnnotationPresent(Test::class.java)
private fun Method.is_before(): Boolean = isAnnotationPresent(Before::class.java)
private fun Method.is_after(): Boolean = isAnnotationPresent(After::class.java)
private fun Method.is_between(): Boolean = isAnnotationPresent(Between::class.java)
private fun Class<*>.hasTests(): Boolean = methods.any{ it.isTest() }

