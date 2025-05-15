package game.doppelkopf.domain.user.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class UserModelTest : BaseUnitTest() {
    @Test
    fun `instantiate with entity from helper method`() {
        val mfp = ModelFactoryProvider()
        val entity = createUserEntity()

        assertThatCode {
            mfp.user.create(entity)
            UserModel(entity, mfp)
        }.doesNotThrowAnyException()
    }
}