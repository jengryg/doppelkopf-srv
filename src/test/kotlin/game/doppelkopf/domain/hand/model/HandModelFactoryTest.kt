package game.doppelkopf.domain.hand.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.model.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class HandModelFactoryTest : BaseUnitTest() {
    @Test
    fun `hand model factory instantiates hand model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createHandEntity()

        val model = mfp.hand.create(entity)
        val other = mfp.hand.create(entity)

        assertThat(model === other).isTrue
    }
}