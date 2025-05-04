package game.doppelkopf.domain.round.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.model.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoundModelFactoryTest : BaseUnitTest() {
    @Test
    fun `round model factory instantiates round model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createRoundEntity()

        val model = mfp.round.create(entity)
        val other = mfp.round.create(entity)

        assertThat(model === other).isTrue
    }
}