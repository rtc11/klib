package util

class Cli(private val args: Array<String>) {
    private var pos = 0
    private var readPos = 0
    private var arg: String = ""

    private fun hasNext() = pos < args.size

    fun parse(): List<Cmd> = buildList {
        while (hasNext()) {
            add(next())
        }
    }

    fun next(): Cmd {
        val cmd = when {
            arg.isEmpty() -> Cmd.empty
            arg.startsWith("-") -> {
                if (peekArg().contains("-")) {
                    Cmd.flag(arg)
                } else {
                    val flag = arg
                    val value = readValue()
                    Cmd.flagWithValue(flag, value)
                }
            }
            else -> Cmd.value(arg)
        }
        read()
        return cmd
    }

    private fun readValue(): String = buildString {
        while (arg.isNotBlank()) {
            append(arg)
            when (peekArg().startsWith("-")) {
                true -> break
                false -> read()
            }
        }
    }

    private fun read() {
        arg = peekArg()
        pos = readPos++
    }

    private fun peekArg() = args.getOrElse(readPos) { "" }

}

sealed interface Cmd {
    data class value(val value: String): Cmd
    data class flag(val flag: String): Cmd
    data class flagWithValue(val flag: String, val value: String): Cmd
    data object empty: Cmd
}
