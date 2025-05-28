package game.doppelkopf.adapter.graphql.core

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.game.service.GameJoinModel
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PlayerGraphQLControllerTest : BaseGraphQLTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class Join {
        @Test
        fun `join game at valid seat position returns player`() {
            val game = gameRepository.save(createGameOfUser(testAdmin))
            val player = PlayerEntity(user = testUser, game = game, seat = 1)

            mockkConstructor(GameJoinModel::class)
            every { anyConstructed<GameJoinModel>().join(any(), 1) } returns mockk {
                every { entity } returns player
            }

            val document = """
                    mutation {
                        joinGame(joinGameInput: { gameId: "${game.id}", seat: 1 }) {
                            id
                        }
                    }
            """.trimIndent()

            gqlUserTester.document(document).execute()
        }
    }

    private fun createGameOfUser(userEntity: UserEntity): GameEntity {
        return GameEntity(
            creator = userEntity, maxNumberOfPlayers = 4, seed = ByteArray(256)
        ).apply {
            players.add(
                PlayerEntity(
                    user = userEntity, game = this, seat = 0
                )
            )
        }
    }
}