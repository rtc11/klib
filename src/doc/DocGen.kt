import java.io.File
import java.io.PrintWriter

/**
 * A simple program to generate man pages from Kotlin source files.
 * This script processes multiple files, generating a summary man page
 * and a separate man page for each class found.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: ./nob doc <file.kt> <file2.kt> ...")
        return
    }

    val man1_dir = File("man1")
    if (!man1_dir.exists()) {
        man1_dir.mkdir()
    }

    val all_files_data = mutableMapOf<String, ManPageData>()
    val entrypoint_data = mutableMapOf<String, String>()

    val class_pattern = """(?:/\*\*(.*?)\*/\s*)?(data\s+)?(class|object)\s+([a-zA-Z0-9_]+)""".toRegex(RegexOption.DOT_MATCHES_ALL)
    val fun_pattern = """^\s*(?:/\*\*\s*\n(.*?)\s*\*/\s*)?fun\s+([a-zA-Z0-9_]+)(?:\s*<.*?>)?\((.*?)\)(?:\s*:\s*[a-zA-Z0-9_<>]+)?""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
    val property_pattern = """^\s*(?:/\*\*\s*\n(.*?)\s*\*/\s*)?(const\s+)?(val|var)\s+([a-zA-Z0-9_]+)\s*:\s*([a-zA-Z0-9_<>]+)""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))

    for (file_path in args) {
        val src_file = File(file_path)
        if (!src_file.exists()) {
            println("Error: File not found at ${src_file.absolutePath}")
            continue
        }

        val source_code = src_file.readText()
        val package_name = source_code.lineSequence().firstOrNull { it.startsWith("package ") }?.removePrefix("package ")?.trim() ?: ""
        val file_data = ManPageData(package_name, src_file.name)

        class_pattern.findAll(source_code).forEach { match ->
            val (comment_group, is_data, type, class_name) = match.destructured
            val comment = comment_group ?: "No description available."
            file_data.classes.add(class_name)
            file_data.class_data[class_name] = ClassData(
                comment = clean_kdoc_comment(comment),
                is_data = is_data.isNotBlank(),
                type = type
            )
        }

        fun_pattern.findAll(source_code).forEach { match ->
            val (comment, signature, _, _) = match.destructured
            file_data.functions.add(signature)

            if (signature.substringBefore('<') == "main") {
                entrypoint_data["name"] = if (file_data.package_name.isNotBlank()) "${file_data.package_name}.$signature" else signature
                entrypoint_data["file"] = src_file.name
                entrypoint_data["kdoc"] = clean_kdoc_comment(comment ?: "No description available.")
            }
        }

        property_pattern.findAll(source_code).forEach { match ->
            val (comment, is_const, _, property_name) = match.destructured
            file_data.globals.add(property_name)
            file_data.global_data[property_name] = GlobalData(
                comment = clean_kdoc_comment(comment ?: "No description available."),
                is_const = is_const.isNotBlank()
            )
        }
        all_files_data[file_path] = file_data
    }

    generate_summary_page(all_files_data, entrypoint_data)

    for ((_, file_data) in all_files_data) {
        for ((class_name, class_data) in file_data.class_data) {
            generate_class_page(class_name, class_data, file_data, all_files_data, entrypoint_data)
        }
    }

    println("All man pages successfully generated.")
}

/** Data class to hold parsed information for a single file. */
data class ManPageData(
    val package_name: String,
    val file_name: String,
    val classes: MutableList<String> = mutableListOf(),
    val functions: MutableList<String> = mutableListOf(),
    val globals: MutableList<String> = mutableListOf(),
    val class_data: MutableMap<String, ClassData> = mutableMapOf(),
    val global_data: MutableMap<String, GlobalData> = mutableMapOf()
)

/** Store specific details about each class */
data class ClassData(val comment: String, val is_data: Boolean, val type: String)
data class GlobalData(val comment: String, val is_const: Boolean)

/**
 * Cleans a KDoc comment by removing leading asterisks and trimming whitespace.
 */
fun clean_kdoc_comment(comment: String): String {
    return comment.lines().joinToString("\n") { line ->
        line.trimStart().removePrefix("*").trim()
    }.trim()
}

fun generate_summary_page(all_files_data: MutableMap<String, ManPageData>, entrypoint_data: MutableMap<String, String>) {
    File("man1", "summary.1").printWriter().use { writer ->
        writer.println(".TH SUMMARY 1")
        writer.println(".SH NAME")
        writer.println("Summary \\- Overview of the Kotlin project.")
        writer.println(".SH DESCRIPTION")
        val main_kdoc = entrypoint_data["kdoc"]
        writer.println(main_kdoc?.replace("\n", "\n.br\n") ?: "No description available.")
        writer.println()
        writer.println(".SH ENTRYPOINT")
        val main_name = entrypoint_data["name"] ?: "No entry point found."
        val main_file = entrypoint_data["file"] ?: "N/A"
        writer.println(".B $main_name")
        writer.println("located in file .I $main_file")


        writer.println(".SH CLASSES")
        for ((_, file_data) in all_files_data) {
            file_data.class_data.forEach { (class_name, class_data) ->
                writer.println(".B ${class_name.lowercase()}(1)")
                writer.println("    ${get_first_sentence(class_data.comment)}")
                writer.println(".br")
            }
        }

        writer.println(".SH FUNCTIONS")
        for ((_, file_data) in all_files_data) {
            file_data.functions.forEach { fun_signature ->
                writer.println(".B ${fun_signature.lowercase()}(1)")
                writer.println(".br")
            }
        }

        writer.println(".SH GLOBALS")
        for ((_, file_data) in all_files_data) {
            file_data.globals.forEach { global_name ->
                writer.println(".B ${global_name.lowercase()}(1)")
                writer.println(".br")
            }
        }
    }
}

fun generate_class_page(class_name: String, class_data: ClassData, file_data: ManPageData, all_files_data: MutableMap<String, ManPageData>, entrypoint_data: MutableMap<String, String>) {
    val output_filename = "${class_name.lowercase()}.1"
    File("man1", output_filename).printWriter().use { writer ->
        writer.println(".TH ${class_name.uppercase()} 1")
        writer.println(".SH NAME")
        writer.println("$class_name \\- ${get_first_sentence(class_data.comment)}")
        writer.println(".SH SYNOPSIS")
        writer.println("${file_data.package_name}.$class_name")
        writer.println(".SH DESCRIPTION")
        writer.println(class_data.comment.replace("\n", "\n.br\n"))
        writer.println()
        writer.println(".SH FUNCTIONS")
        file_data.functions.forEach { fun_signature ->
            writer.println(".B ${fun_signature.lowercase()}(1)")
            writer.println(".br")
        }
        writer.println(".SH GLOBALS")
        file_data.globals.forEach { global_name ->
            writer.println(".B ${global_name.lowercase()}(1)")
            writer.println(".br")
        }
    }
}

/** Extracts the first sentence from a KDoc comment. */
fun get_first_sentence(comment: String): String {
    val period_idx = comment.indexOf('.')
    return if (period_idx != -1) {
        comment.substring(0, period_idx + 1).trim().replace("\n", " ")
    } else {
        comment.trim().replace("\n", " ")
    }
}

