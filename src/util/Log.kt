package util

import java.time.*
import java.time.format.*
import java.time.temporal.*
import java.util.logging.*

private val handler =
        ConsoleHandler().apply {
            formatter = ConsoleFormatter()
            level = Logger.level
            encoding = "UTF-8"
        }

private val loggers = mutableMapOf<String, Logger>()

class Logger internal constructor(name: String) {
    private val log = java.util.logging.Logger.getLogger(name).also { it.addHandler(handler) }

    fun trace(msg: Any) = log.finer("$msg")
    fun debug(msg: Any) = log.fine("$msg")
    fun info(msg: Any) = log.info("$msg")
    fun warn(msg: Any) = log.warning("$msg")
    fun error(msg: Any) = log.severe("$msg")
    fun error(msg: String, e: Throwable) = log.log(Level.SEVERE, msg, e)

    companion object {
        var level: Level = Level.ALL

        /** Logger.load("/log.conf") is required to use custom loggers */
        fun load(path: String) =
                Resource.input(path).let { LogManager.getLogManager().readConfiguration(it) }
    }
}

fun logger(name: String) = loggers[name] ?: Logger(name).also { loggers[name] = it }

object ANSI {
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val BRIGHT_RED = "\u001B[91m"
    const val GREEN = "\u001B[32m"
    const val BRIGHT_GREEN = "\u001B[92m"
    const val YELLOW = "\u001B[33m"
    const val BRIGHT_YELLOW = "\u001B[93m"
    const val BLUE = "\u001B[34m"
    const val BRIGHT_BLUE = "\u001B[94m"
    const val MAGENTA = "\u001B[35m"
    const val BRIGHT_MAGENTA = "\u001B[95m"
    const val CYAN = "\u001B[36m"
    const val BRIGHT_CYAN = "\u001B[96m"
    const val WHITE = "\u001B[37m"
    const val BRIGHT_WHITE = "\u001B[97m"
    const val RESET = "\u001B[0m"
}

// ! not supported by IntelliJ console
object ANSI24bit {
    // foreground color, also called text, r:0-255 g:0-255 b:0-255
    fun fg(r: Int, g: Int, b: Int): String = "\u001B[38;2;$r;$g;$b;m"
    fun bg(r: Int, g: Int, b: Int): String = "\u001B[48;2;$r;$g;$b;m"
    fun reset(): String = "\u001B[0m"
}

class ConsoleFormatter : Formatter() {
    private val isoFixedNanoLength =
            DateTimeFormatterBuilder()
                    .appendValue(ChronoField.HOUR_OF_DAY, 2)
                    .appendLiteral(':')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .optionalStart()
                    .appendLiteral(':')
                    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true)
                    .toFormatter()

    private val date
        get() = isoFixedNanoLength.format(LocalDateTime.now())
    override fun format(record: LogRecord): String {
        val msg = formatMessage(record)
        val (lvlColor, lvlName) = record.level.colorAndName()
        return "${ANSI.CYAN}$date ${ANSI.WHITE}[${record.longThreadID}] $lvlColor$lvlName ${ANSI.YELLOW}${record.loggerName} ${ANSI.BRIGHT_WHITE}$msg\n${ANSI.RESET}"
    }

    private fun Level.colorAndName(): Pair<String, String> =
            when (this.name) {
                //        "OFF" -> {}
                "SEVERE" -> ANSI.BRIGHT_RED to "ERROR"
                "WARNING" -> ANSI.BRIGHT_YELLOW to "WARN "
                "INFO" -> ANSI.BRIGHT_GREEN to "INFO "
                "FINE" -> ANSI.WHITE to "DEBUG"
                "FINER" -> ANSI.CYAN to "TRACE"
                //        "FINEST" -> {}
                //        "ALL" -> {}
                else -> ANSI.BRIGHT_WHITE to name
            }
}

