package test

import java.io.File
import kotlin.io.println
import util.*

fun main(args: Array<String>) {
    val pkg = args[0]
    ClassLoader.findClasses(pkg)
}

@Target(AnnotationTarget.FUNCTION) 
annotation class Test

object ClassLoader {
    fun findClasses(pkg: String) {
        val name = "/${pkg.replace(".", "/")}"
        val url = ClassLoader::class.java.getResource(name)
        val dir = File(url.getFile())

        if (dir.exists()) {
            if (dir.isDirectory) {
                dir.walk().filter{ it.name.contains('$') == false && it.extension == "class" }
                .map { pkg + it.canonicalPath.removePrefix(dir.canonicalPath).dropLast(6).replace("/", ".") }
                .map { Class.forName(it) }
                .filterNot { it.isAnnotation() }
                .filterNot { it.isInterface }
                .filterNot { it.isEnum }
                .filterNot { it.declaredConstructors.isEmpty() }
                .filterNot { it.isHidden }
                .filterNot { it.isAnonymousClass }
                .forEach {
                    try {
                        val clazz = it.getDeclaredConstructor().newInstance()
                        clazz::class.java.methods
                            .filter { it.isAnnotationPresent(Test::class.java) }
                            .map { 
                                val start = System.nanoTime()
                                try {
                                    it.invoke(clazz)
                                    val timedMs = (System.nanoTime() - start) / 1_000_000
                                    timedMs to "${ANSI.BRIGHT_GREEN}[SUCCESS]${ANSI.RESET} ${it.name}"
                                } catch (e: AssertionError) {
                                    val timedMs = (System.nanoTime() - start) / 1_000_000
                                    timedMs to "${ANSI.BRIGHT_RED}[FAILURE]${ANSI.RESET} ${it.name}"
                                }
                            }.forEach { (ms, status) -> 
                                println("$status [${ms}ms]") 
                            }
                    } catch (cnf: ClassNotFoundException) {
                        println("Class not found: $it")
                    } catch (i: InstantiationException) {
                        println("No constructor: $it")
                    } catch (ia: IllegalAccessException) {
                        println("class is not public: $it")
                    } catch (e: Exception) {
                        println("Exception: $e")
                    }
                }
            }
        }
    }
}
