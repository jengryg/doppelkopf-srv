package game.doppelkopf.domain.turn.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TurnModelFactoryTest : BaseUnitTest() {
    @Test
    fun `trick model factory instantiates trick model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createTurnEntity()

        val model = mfp.turn.create(entity)
        val other = mfp.turn.create(entity)

        assertThat(model === other).isTrue
    }
}