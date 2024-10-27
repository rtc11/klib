package util 

import test.*
import util.*
import util.*

class CommandsTest {

    @Test
    fun `parse no args`() {
        val cli = Commands(arrayOf())
        val cmds = cli.parse()
        assertEq(cmds.size, 0)
    }

    @Test
    fun `parse double dashed flags`() {
        val cli = Commands(arrayOf("--version"))
        val cmds = cli.parse()
        assertEq(cmds.size, 1)
        val cmd = cmds.single()
        assertIs<Cmd.Flag>(cmd)
        assert<Cmd.Flag>(cmd) { it.flag == "--version" }
    }

    @Test
    fun `parse double dashed flag with value`() {
        val cli = Commands(arrayOf("--version", "1"))
        val cmds = cli.parse()
        assertEq(cmds.size, 1)
        val cmd = cmds.single()
        assertIs<Cmd.FlagWithValue>(cmd)
        assert<Cmd.FlagWithValue>(cmd) { it.flag == "--version" }
        assert<Cmd.FlagWithValue>(cmd) { it.value == "1" }
    }

    @Test
    fun `parse one flag`() {
        val cli = Commands(arrayOf("-v"))
        val cmds = cli.parse()
        assertEq(cmds.size, 1)
        val cmd = cmds.single()
        assertIs<Cmd.Flag>(cmd)
        assert<Cmd.Flag>(cmd) { it.flag == "-v" }
    }

    @Test
    fun `parse multiple flags`() {
        val cli = Commands(arrayOf("-a", "-b", "-c"))
        val cmds = cli.parse()
        assertEq(cmds.size, 3)
        val a = cmds[0]
        val b = cmds[1]
        val c = cmds[2]
        assertIs<Cmd.Flag>(a)
        assertIs<Cmd.Flag>(b)
        assertIs<Cmd.Flag>(c)
        assert<Cmd.Flag>(a) { it.flag == "-a" }
        assert<Cmd.Flag>(b) { it.flag == "-b" }
        assert<Cmd.Flag>(c) { it.flag == "-c" }
    }

    @Test
    fun `parse flag with value`() {
        val cli = Commands(arrayOf("-c", "class"))
        val cmds = cli.parse()
        assertEq(cmds.size, 1)
        val cmd = cmds.single()
        assertIs<Cmd.FlagWithValue>(cmd)
        assert<Cmd.FlagWithValue>(cmd) { it.flag == "-c"}
        assert<Cmd.FlagWithValue>(cmd) { it.value == "class" }
    }

    @Test
    fun `parse multiple flags with value`() {
        val cli = Commands(arrayOf("-c", "class", "-t", "test"))
        val cmds = cli.parse()
        assertEq(cmds.size, 2)
        val c = cmds[0]
        val t = cmds[1]
        assertIs<Cmd.FlagWithValue>(c)
        assertIs<Cmd.FlagWithValue>(t)
        assert<Cmd.FlagWithValue>(c) { it.flag == "-c"}
        assert<Cmd.FlagWithValue>(c) { it.value == "class" }
        assert<Cmd.FlagWithValue>(t) { it.flag == "-t"}
        assert<Cmd.FlagWithValue>(t) { it.value == "test" }
    }

    @Test
    fun `accumulates subsequent flag values`() {
        val cli = Commands(arrayOf("-c", "class", "name", "test"))
        val cmds = cli.parse()
        assertEq(cmds.size, 1)
        val c = cmds[0]
        assertIs<Cmd.FlagWithValue>(c)
        assert<Cmd.FlagWithValue>(c) { it.flag == "-c"}
        assert<Cmd.FlagWithValue>(c) { it.value == "class name test" }
    }
}

