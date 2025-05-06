package game.doppelkopf.utils

import game.doppelkopf.BaseUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TeamedTest : BaseUnitTest() {
    @Test
    fun `decompose returns re first, ko second`() {
        val teamed = Teamed(1, 2)

        val (x1, x2) = teamed

        assertThat(x1).isEqualTo(1)
        assertThat(x2).isEqualTo(2)
    }

    @Test
    fun `map applies block to re and ko in correct tagging`() {
        val teamed = Teamed(1, 2)

        val mapped = teamed.map { it + 17 }

        assertThat(mapped.re).isEqualTo(18)
        assertThat(mapped.ko).isEqualTo(19)
    }

    @Test
    fun `map tagged applies block to re and ko in correct tagging`() {
        val teamed = Teamed(1, 2)

        val mapped = teamed.mapTagged { team, it ->
            "$team : $it"
        }

        assertThat(mapped.re).isEqualTo("RE : 1")
        assertThat(mapped.ko).isEqualTo("KO : 2")
    }

    @Test
    fun `map with other applies block to re and ko in correct tagging and argument order`() {
        val teamed = Teamed(1, 2)
        val other = Teamed(3, 4)

        val mapped = teamed.map(other) { x, y ->
            y * y - x * x
        }

        assertThat(mapped.re).isEqualTo(8)
        assertThat(mapped.ko).isEqualTo(12)
    }

    @Test
    fun `map tagged with other applies block to re and ko in correct tagging and argument order`() {
        val teamed = Teamed(1, 2)
        val other = Teamed(3, 4)

        val mapped = teamed.mapTagged(other) { team, x, y ->
            "$team : ( $x , $y )"
        }

        assertThat(mapped.re).isEqualTo("RE : ( 1 , 3 )")
        assertThat(mapped.ko).isEqualTo("KO : ( 2 , 4 )")
    }
}