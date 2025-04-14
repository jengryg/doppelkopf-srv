package game.doppelkopf.core.model.user

import game.doppelkopf.persistence.model.user.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserModelTest {
    @Nested
    inner class Create {
        @Test
        fun `create uses one model per entity`() {
            val entity = UserEntity(
                username = "username",
                password = "password"
            )

            val model = UserModel.create(entity)
            val other = UserModel.create(entity)

            assertThat(model).isSameAs(other)
        }
    }
}