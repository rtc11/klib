package nob

import test.*
import util.*
import nob.*

class NobTest {

    @Test
    fun `default classpath`() {
        val module = Module()
        val cwd = System.getProperty("user.dir")
        eq(java.nio.file.Paths.get(cwd, "out").toString(), module.compile_cp())
    }
}
