package game.doppelkopf.domain.call.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CallModelFactoryTest : BaseUnitTest() {
    @Test
    fun `call model factory instantiates call model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createCallEntity()

        val model = mfp.call.create(entity)
        val other = mfp.call.create(entity)

        Assertions.assertThat(model === other).isTrue
    }
}