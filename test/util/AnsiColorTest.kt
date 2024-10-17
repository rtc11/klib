package util

import org.junit.jupiter.api.*

class AnsiColorTest {

    @Test
    fun `24 bit ansi color`() {
        val sandyBrown = ANSI24bit.bg(255, 179, 102)
        val lincolnGreen = ANSI24bit.fg(34, 102, 0)
        val text = "color test"
        println("$sandyBrown$lincolnGreen$text${ANSI24bit.reset()}")
        println("$lincolnGreen$text${ANSI24bit.reset()}")
    }

    @Test
    fun `4 bit ansi color`() {
        val text = "color test"
        println("${ANSI.CYAN}$text${ANSI24bit.reset()}")
    }
}
