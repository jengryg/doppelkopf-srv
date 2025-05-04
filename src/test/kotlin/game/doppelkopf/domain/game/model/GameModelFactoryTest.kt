package game.doppelkopf.domain.game.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
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