package game.doppelkopf.domain.hand.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class HandModelFactoryTest : BaseUnitTest() {
    @Test
    fun `hand model factory instantiates hand model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createHandEntity()

        val model = mfp.hand.create(entity)
        val other = mfp.hand.create(entity)

        Assertions.assertThat(model === other).isTrue
    }
}