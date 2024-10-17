package server

import org.junit.jupiter.api.*
import util.*

class PathTest {
    @Test
    fun `can be empty string`() {
        Path("").match("").unwrap()
    }

    @Test
    fun `can be atomic`() {
        Path("hello").match("hello").unwrap()
    }

    @Test
    fun `can contain slashes`() {
        Path("hello/you/there").match("hello/you/there").unwrap()
    }

    @Test
    fun `can have param`() {
        Path("hello/:id").match("hello/r1").unwrap()
    }

    @Test
    fun `can have multiple param`() {
        Path("hello/:id/:name").match("hello/r1/b2").unwrap()
    }
}