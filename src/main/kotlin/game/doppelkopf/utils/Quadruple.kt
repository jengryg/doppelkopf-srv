package game.doppelkopf.utils

/**
 * [Quadruple] represents 4 of the given type [T]. All 4 must be the same type.
 */
data class Quadruple<T>(
    val first: T,
    val second: T,
    val third: T,
    val fourth: T,
) {
    /**
     * @return list in the order [first], [second], [third], [fourth]
     */
    fun toList(): List<T> = listOf(first, second, third, fourth)

    /**
     * @Return a [Quadruple] containing the result of [block] applied to each of the four components in order
     */
    fun <S> map(block: (T) -> S): Quadruple<S> {
        return Quadruple(block(first), block(second), block(third), block(fourth))
    }

    /**
     * @return a [Quadruple] containing the result of [block] applied to each of the four pairs of components in order,
     * where the first argument supplied to block is from this [Quadruple] and the second argument is from [other].
     */
    fun <S, U> map(other: Quadruple<U>, block: (T, U) -> S): Quadruple<S> {
        return Quadruple(
            block(first, other.first),
            block(second, other.second),
            block(third, other.third),
            block(fourth, other.fourth)
        )
    }
}