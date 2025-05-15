package game.doppelkopf.domain.game.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class GameModelTest : BaseUnitTest() {
    @Test
    fun `instantiate with entity from helper method`() {
        val mfp = ModelFactoryProvider()
        val entity = createGameEntity()

        assertThatCode {
            mfp.game.create(entity)
            GameModel(entity, mfp)
        }.doesNotThrowAnyException()
    }
}