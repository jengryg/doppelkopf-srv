package game.doppelkopf.core.model.player

import game.doppelkopf.BaseUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlayerModelTest : BaseUnitTest() {
    @Nested
    inner class Create {
        @Test
        fun `create uses one model per entity`() {
            val entity = createPlayerEntity()

            val model = PlayerModel.create(entity)
            val other = PlayerModel.create(entity)

            assertThat(model).isSameAs(other)
        }
    }
}