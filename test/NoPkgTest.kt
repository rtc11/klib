import test.*

class NoPkgTest {
    private var left = 1
    private var right = 2

    @Test
    fun `can test classes without package`() {
        eq(left, right)
    }

    @Before
    fun setup() {
        right = 1
    }

    @After
    fun cleanup() {
        right = 2
    }
}

