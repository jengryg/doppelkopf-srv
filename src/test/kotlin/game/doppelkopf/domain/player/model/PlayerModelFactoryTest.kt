package game.doppelkopf.domain.player.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerModelFactoryTest : BaseUnitTest() {
    @Test
    fun `player model factory instantiates player model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createPlayerEntity()

        val model = mfp.player.create(entity)
        val other = mfp.player.create(entity)

        assertThat(model === other).isTrue
    }
}