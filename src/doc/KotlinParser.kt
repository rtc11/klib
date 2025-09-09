package doc

import parser.*
import util.*

data class ParsedFile(val classes: List<ParsedClass>, val functions: List<ParsedFunction>, val globals: List<ParsedGlobal>)
data class ParsedClass(val name: String, val kdoc: String?, val kind: String)
data class ParsedFunction(val name: String, val kdoc: String?, val signature: String, val modifier: String?)
data class ParsedGlobal(val name: String, val kdoc: String?, val is_const: Boolean, val value: String)

object KotlinParser {

    private val identifier = parser { 
        takeWhile { it.isLetterOrDigit() || it == '_' }
    }

    private val kdoc: Parser<String?> = optional(parser {
        blanks()
        str("/**")
        val docText = takeUntil("*/")
        str("*/")
        docText.trim().lines()
            .filter { it.trim().trimStart('*').trim().isNotEmpty() }
            .joinToString("\n") { it.trimStart().trimStart('*').trim() }
            .trim()
    })

    private fun State.parse_braced_body(): String {
        var brace_depth = 1
        val start_pos = pos
        while (pos < input.length) {
            when (input[pos]) {
                '{' -> brace_depth++
                '}' -> brace_depth--
            }
            pos++
            if (brace_depth == 0) break
        }
        if (brace_depth != 0) fail("Unmatched braces in block.")
        return input.substring(start_pos, pos - 1).trim()
    }

    private val function_modifiers: Parser<String> = oneOf(
        parser { str("private"); blanks() }, 
        parser { str("public"); blanks() }, 
        parser { str("internal"); blanks() }, 
        parser { str("inline"); blanks() }, 
        parser { str("infix"); blanks() }, 
        parser { str("operator"); blanks() }, 
        parser { str("suspend"); blanks() }, 
        parser { str("override"); blanks() }, 
    )

    private fun parse_function(doc: String?): Parser<ParsedFunction> = parser {
        blanks()
        
        val modifier_start_pos = pos
        optional(parser {
            many1(function_modifiers)
            blanks()
        })()
        val modifiers = input.substring(modifier_start_pos, pos).trim().ifEmpty { null } 

        str("fun")
        blanks()
        
        val name_and_signature = takeUntilOneOf("{", "=").trim()
        val name = name_and_signature.split('(', limit = 2).first().trim()
        
        blanks()
        oneOf(
            parser { char('{'); parse_braced_body() },
            parser { char('='); takeUntil("\n") },
            parser { takeUntil("\n") },
        )

        ParsedFunction(name, doc, name_and_signature, modifiers)
    }

    private fun class_or_object(doc: String?): Parser<ParsedClass> = parser {
        blanks()
        val kind = oneOf(
            parser { str("data class") },
            parser { str("data object") },
            parser { str("value class") },
            parser { str("class") },
            parser { str("object") },
        )
        blanks()
        val name = identifier()

        optional(parser {
            blanks()
            char('(')
            takeUntil(")")
            char(')')
        })()

        optional(parser {
            blanks()
            char('{')
            parse_braced_body()
            blanks()
        })()
        
        ParsedClass(name, doc, kind)
    }

    private fun parse_global(doc: String?): Parser<ParsedGlobal> = parser {
        blanks()
        val is_const = optional(parser { str("const"); blanks() })() != null
        str("val")
        blanks()
        val name = takeUntil("=").trim()
        char('=')
        blanks()
        val value = if (next == '{') {
            char('{')
            parse_braced_body()
        } else {
            takeUntil("\n").trim()
        }
        ParsedGlobal(name, doc, is_const, value)
    }

    private val topLevelDeclaration: Parser<Any?> = parser {
        blanks()
        val doc = kdoc()
        blanks()
        oneOf(
            class_or_object(doc),
            parse_function(doc),
            parse_global(doc),
        )
    }

    private val import_stmt: Parser<String> = parser {
        str("import")
        val import_path = takeUntil("\n")
        blanks()
        import_path
    }

    private val parser: Parser<ParsedFile> = parser {
        optional(parser { str("package"); takeUntil("\n") })()
        blanks()
        many(import_stmt)

        // val declarations = many(topLevelDeclaration)
        // val classes = declarations.filterIsInstance<ParsedClass>()
        // val functions = declarations.filterIsInstance<ParsedFunction>()
        // val globals = declarations.filterIsInstance<ParsedGlobal>()
            
        val classes = mutableListOf<ParsedClass>()
        val functions = mutableListOf<ParsedFunction>()
        val globals = mutableListOf<ParsedGlobal>()

        while (reminding.isNotEmpty()) {
            val start = pos
            blanks()
            try {
                val declaration = topLevelDeclaration()
                when (declaration) {
                    is ParsedClass -> classes += declaration
                    is ParsedFunction -> functions += declaration
                    is ParsedGlobal -> globals += declaration
                }
            } catch (e: ParseException) {
                val newline_idx = input.indexOf('\n', pos)
                pos = if (newline_idx != -1) newline_idx + 1 else input.length
            }

            if (pos == start && reminding.isNotEmpty()) pos++
        }

        ParsedFile(classes, functions, globals)
    }

    fun parse(input: String): Result<ParseResult<ParsedFile>, ParseException> {
        val result = parser.parseToEnd(input)
        return result 
    }
}

