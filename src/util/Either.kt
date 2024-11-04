package util

sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()

    fun isLeft() = this is Left
    fun isRight() = this is Right

    fun getLeft() = (this as Left).value
    fun getRight() = (this as Right).value

    fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T = when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

    companion object {
        fun <L> left(value: L) = Left(value)
        fun <R> right(value: R) = Right(value)
    }
}

