package game.doppelkopf.domain.game.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions
import kotlin.test.Test

class GameModelFactoryTest : BaseUnitTest() {
    @Test
    fun `game model factory instantiates game model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createGameEntity()

        val model = mfp.game.create(entity)
        val other = mfp.game.create(entity)

        Assertions.assertThat(model === other).isTrue
    }
}