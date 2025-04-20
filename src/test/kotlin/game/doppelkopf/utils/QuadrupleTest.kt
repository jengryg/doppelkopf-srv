package game.doppelkopf.utils

import game.doppelkopf.BaseUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QuadrupleTest : BaseUnitTest() {
    @Test
    fun `toList returns correct order of elements`() {
        val quad = Quadruple(1, 2, 3, 4)

        assertThat(quad.toList()).containsExactly(1, 2, 3, 4)
    }

    @Test
    fun `decompose returns correct order of elements`() {
        val quad = Quadruple(1, 2, 3, 4)

        val (x1, x2, x3, x4) = quad

        assertThat(x1).isEqualTo(1)
        assertThat(x2).isEqualTo(2)
        assertThat(x3).isEqualTo(3)
        assertThat(x4).isEqualTo(4)
    }

    @Test
    fun `map applies block to each element in correct order`() {
        val quad = Quadruple(1, 2, 3, 4)

        val mapped = quad.map {
            it + 17
        }

        assertThat(mapped.toList()).containsExactly(18, 19, 20, 21)
    }

    @Test
    fun `map indexed applied block to each element in correct order with correct index`() {
        val quad = Quadruple("a", "b", "c", "d")

        val mapped = quad.mapIndexed { index, it ->
            "$index : $it"
        }

        assertThat(mapped.toList()).containsExactly("0 : a", "1 : b", "2 : c", "3 : d")
    }

    @Test
    fun `map with other applies block to each pair of elements in correct order`() {
        val quad = Quadruple(1, 2, 3, 4)
        val other = Quadruple(5, 6, 7, 8)

        val mapped = quad.map(other) { x, y ->
            y * y - x * x
        }

        assertThat(mapped.toList()).containsExactly(24, 32, 40, 48)
    }

    @Test
    fun `map indexed with other applied block to each pair of elements in correct order with correct index`() {
        val quad = Quadruple("a", "b", "c", "d")
        val other = Quadruple("w", "x", "y", "z")

        val mapped = quad.mapIndexed(other) { index, x, y ->
            "$index : ( $x , $y )"
        }

        assertThat(mapped.toList()).containsExactly("0 : ( a , w )", "1 : ( b , x )", "2 : ( c , y )", "3 : ( d , z )")
    }
}