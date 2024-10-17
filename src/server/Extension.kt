package server

sealed interface Extension {
    fun activate()
}

data object OAuth : Extension {
    override fun activate() {
        unauthorized("You are not permitted.")
    }
}
