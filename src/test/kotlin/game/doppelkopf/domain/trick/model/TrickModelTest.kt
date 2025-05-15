package game.doppelkopf.domain.trick.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class TrickModelTest : BaseUnitTest() {
    @Test
    fun `instantiate with entity from helper method`() {
        val mfp = ModelFactoryProvider()
        val entity = createTrickEntity()

        assertThatCode {
            mfp.trick.create(entity)
            TrickModel(entity, mfp)
        }.doesNotThrowAnyException()
    }
}