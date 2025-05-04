package game.doppelkopf.domain.trick.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class TrickModelFactoryTest : BaseUnitTest() {
    @Test
    fun `trick model factory instantiates trick model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createTrickEntity()

        val model = mfp.trick.create(entity)
        val other = mfp.trick.create(entity)

        Assertions.assertThat(model === other).isTrue
    }
}