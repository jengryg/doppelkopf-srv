package game.doppelkopf.domain.user.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserModelFactoryTest : BaseUnitTest() {
    @Test
    fun `user model factory instantiates user model and uses caching`() {
        val mfp = ModelFactoryProvider()

        val entity = createUserEntity()

        val model = mfp.user.create(entity)
        val other = mfp.user.create(entity)

        assertThat(model === other).isTrue
    }
}