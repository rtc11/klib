package util

import test.Test
import util.*

class AnsiColorTest {

    val log = logger("test")

    @Test
    fun `24 bit ansi color`() {
        val sandyBrown = ANSI24bit.bg(255, 179, 102)
        val lincolnGreen = ANSI24bit.fg(34, 102, 0)
        val text = "color test"
        log.info("$sandyBrown$lincolnGreen$text${ANSI24bit.reset()}")
        log.info("$lincolnGreen$text${ANSI24bit.reset()}")
    }

    @Test
    fun `4 bit ansi color`() {
        val text = "color test"
        log.info("${ANSI.CYAN}$text${ANSI24bit.reset()}")
    }
}
