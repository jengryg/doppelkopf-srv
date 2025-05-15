package game.doppelkopf.domain.hand.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class HandModelTest : BaseUnitTest() {
    @Test
    fun `instantiate with entity from helper method`() {
        val mfp = ModelFactoryProvider()
        val entity = createHandEntity()

        assertThatCode {
            mfp.hand.create(entity)
            HandModel(entity, mfp)
        }.doesNotThrowAnyException()
    }
}