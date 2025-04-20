package game.doppelkopf.core.model.user

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.persistence.model.user.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserModelTest : BaseUnitTest() {
    @Nested
    inner class Create {
        @Test
        fun `create uses one model per entity`() {
            val entity = createUserEntity()

            val model = UserModel.create(entity)
            val other = UserModel.create(entity)

            assertThat(model).isSameAs(other)
        }
    }
}