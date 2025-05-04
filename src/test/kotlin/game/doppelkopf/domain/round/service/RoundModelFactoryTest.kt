package game.doppelkopf.domain.round.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class RoundModelFactoryTest : BaseUnitTest() {
    @Test
    fun `round model factory instantiates round model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createRoundEntity()

        val model = mfp.round.create(entity)
        val other = mfp.round.create(entity)

        Assertions.assertThat(model === other).isTrue
    }
}