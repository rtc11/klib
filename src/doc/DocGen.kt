package doc

import util.Result
import java.io.File
import kotlin.system.exitProcess

data class KDocData(val filename: String, val parsed: ParsedFile) 

/** Program to generate man pages for provided kotlin files */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: ./nob doc <file.kt> <file2.kt> ...")
        exitProcess(1)
    }

    val files = args.map { File(it) }.filter { it.exists() }
    val fileDataList = mutableListOf<KDocData>()

    for (file in files) {
        val source = file.readText()
        when (val parsed = KotlinParser.parse(source)) {
            is Result.Ok -> {
                fileDataList.add(KDocData(
                    filename = file.nameWithoutExtension,
                    parsed = parsed.value.result
                ))
            }
            is Result.Err -> {
                val err = parsed.error
                println("Failed to parse ${file.name}: ${err.reason}")
                println("  At line ${err.line}, column ${err.col}")
            }
        }
    }

    generate_summary_page(fileDataList)
    fileDataList
        .filterNot { it.parsed.functions.any { it.name == "main" } }
        .forEach(::generate_class_page)
}

/** Generates a summary named after the file containing main() */
fun generate_summary_page(files: List<KDocData>) {
    val main_file = files.firstOrNull { it.parsed.functions.any { f -> f.name == "main" } } ?: return
    val sb = StringBuilder()
    sb.appendLine(".TH SUMMARY 1")
    sb.appendLine(".SH DESCRIPTION")
    sb.appendLine(main_file.parsed.functions.firstOrNull { it.name == "main" }?.kdoc ?: "No description available.")

    sb.appendLine()
    sb.appendLine(".SH CLASSES")
    files.forEach { f ->
        f.parsed.classes.forEach { c ->
            sb.appendLine(".B ${c.name}(1)")
            if (!c.kdoc.isNullOrBlank()) {
                sb.appendLine("    ${c.kdoc}")
                sb.appendLine(".br")
            }
        }
    }

    sb.appendLine(".SH FUNCTIONS")
    files.forEach { f ->
        f.parsed.functions.forEach { fn ->
            sb.appendLine(".B ${fn.name}(1)")
            fn.kdoc?.let { sb.appendLine("    $it")}
            sb.appendLine(".br") 
        }
    }

    sb.appendLine(".SH GLOBALS")
    files.forEach { f ->
        f.parsed.globals.forEach { g ->
            sb.appendLine("${g.name} = ${g.value}")
            g.kdoc?.let { sb.appendLine("    $it")}
            sb.appendLine(".br") 
        }
    }

    File("man1/${main_file.filename.lowercase()}.1").apply {
        parentFile.mkdirs()
        writeText(sb.toString())
    }.also {
        println("generated $it")
    }
}

/** Generates one page per file */
fun generate_class_page(file: KDocData) {
    val sb = StringBuilder()
    sb.appendLine(".TH ${file.filename.uppercase()} 1")
    sb.appendLine(".SH NAME")
    sb.appendLine("${file.filename} \\- ${file.parsed.functions.firstOrNull{ it.name == "main" }?.kdoc ?: "No description available"}")
    sb.appendLine(".SH SYNOPSIS")
    sb.appendLine("${file.filename.lowercase()}.${file.filename}")
    sb.appendLine()

    sb.appendLine(".SH CLASSES")
    file.parsed.classes.forEach { c ->
        sb.appendLine(".B ${c.name}(1)")
        if (!c.kdoc.isNullOrBlank()) sb.appendLine("    ${c.kdoc}").appendLine(".br")
    }

    sb.appendLine(".SH FUNCTIONS")
    file.parsed.functions.forEach { fn ->
        sb.appendLine(".B ${fn.name}(1)")
        fn.kdoc?.let { sb.appendLine("    $it").appendLine(".br") }
    }

    sb.appendLine(".SH GLOBALS")
    file.parsed.globals.forEach { g ->
        sb.appendLine(".B ${g.name}(1)")
        g.kdoc?.let { sb.appendLine("    $it").appendLine(".br") }
    }

    File("man1/${file.filename.lowercase()}.1").apply {
        parentFile.mkdirs()
        writeText(sb.toString())
    }.also {
        println("generated $it")
    }
}

