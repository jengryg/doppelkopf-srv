package game.doppelkopf.domain.call.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThatCode
import kotlin.test.Test

class CallModelTest : BaseUnitTest() {
    @Test
    fun `instantiate with entity from helper method`() {
        val mfp = ModelFactoryProvider()
        val entity = createCallEntity()

        assertThatCode {
            mfp.call.create(entity)
            CallModel(entity, mfp)
        }.doesNotThrowAnyException()
    }
}