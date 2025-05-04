package game.doppelkopf.domain.trick.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TrickModelFactoryTest : BaseUnitTest() {
    @Test
    fun `trick model factory instantiates trick model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createTrickEntity()

        val model = mfp.trick.create(entity)
        val other = mfp.trick.create(entity)

        assertThat(model === other).isTrue
    }
}