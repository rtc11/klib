import test.*

class NoPkgTest {

    @Test
    fun `can test classes without package`() {
        assertEq(1, 1)
    }
}

