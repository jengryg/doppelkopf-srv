package game.doppelkopf.core.model.player

import game.doppelkopf.persistence.model.player.PlayerEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlayerModelTest {
    @Nested
    inner class Create {
        @Test
        fun `create uses one model per entity`() {
            val entity = PlayerEntity(
                user = mockk(),
                game = mockk(),
                seat = 0
            )

            val model = PlayerModel.create(entity)
            val other = PlayerModel.create(entity)

            assertThat(model).isSameAs(other)
        }
    }
}