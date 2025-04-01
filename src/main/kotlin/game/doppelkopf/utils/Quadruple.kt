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
}