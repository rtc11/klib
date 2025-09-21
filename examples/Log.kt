package examples

import util.*

fun main() {
    // Used to configure custom console logger
    Logger.load("/log.conf")

    // Optionally set log level restriction
//    Logger.level = Level.INFO

    val spam = logger("spammer")
    val important = logger("important")

    spam.trace("hello")
    spam.debug("hello")
    spam.info("hello")
    spam.warn("hello")
    spam.error("hello")

    important.trace("hello")
    important.debug("hello")
    important.info("hello")
    important.warn("hello")
    important.error("hello")
}

