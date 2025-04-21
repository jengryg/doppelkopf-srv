package game.doppelkopf.core.model.game

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.model.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class GameModelFactoryTest : BaseUnitTest() {
    @Test
    fun `game model factory instantiates game model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createGameEntity()

        val model = mfp.game.create(entity)
        val other = mfp.game.create(entity)

        assertThat(model === other).isTrue
    }
}