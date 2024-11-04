package util

fun List<Cmd>.getFlagValue(flag: String): String? {
    return this.filterIsInstance<Cmd.FlagWithValue>().find { it.flag == flag }?.value
}

fun List<Cmd>.hasFlag(vararg flag: String): Boolean {
    return this.filterIsInstance<Cmd.Flag>().any { it.flag in flag }
} 


class Commands(private val args: Array<String>) {
    private var pos = 0
    private var arg: String = args.getOrElse(pos) { "" }

    private fun hasNext() = pos + 1 < args.size 

    fun parse(): List<Cmd> = buildList {
        while (true) {
            next()?.let { add(it) } 
                ?: break
        }
    }

    fun next(): Cmd? {
        val cmd = when {
            arg.isEmpty() -> null
            arg.startsWith("-") -> {
                if (peekArg().isBlank() || peekArg().startsWith("-")) {
                    Cmd.Flag(arg)
                } else {
                    val flag = arg
                    val value = readValue()
                    Cmd.FlagWithValue(flag, value)
                }
            }
            else -> null
        }
        read()
        return cmd
    }

    private fun readValue(): String = buildString {
        while (arg.isNotBlank()) {
            when (peekArg().startsWith("-")) {
                true -> break
                false -> read() // todo: trenger jeg å sjekke peekArg().isBlank()?
            }
            append("$arg ")
        }
    }.trim() // remove last space

    private fun read() {
        arg = peekArg()
        pos++
    }

    private fun peekArg(): String = args.getOrElse(pos+1) { "" }
}

sealed interface Cmd {
    data class Flag(val flag: String): Cmd
    data class FlagWithValue(val flag: String, val value: String): Cmd
}

