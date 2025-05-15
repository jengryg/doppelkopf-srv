package game.doppelkopf.domain.player.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class PlayerModelTest : BaseUnitTest() {
    @Test
    fun `instantiate with entity from helper method`() {
        val mfp = ModelFactoryProvider()
        val entity = createPlayerEntity()

        assertThatCode {
            mfp.player.create(entity)
            PlayerModel(entity, mfp)
        }.doesNotThrowAnyException()
    }
}