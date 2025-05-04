package game.doppelkopf.core

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.domain.game.handler.GameStartModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class GameFacade(
    private val gamePersistence: GamePersistence
) {
    /**
     * Create a new game that is owned by [user].
     *
     * @param playerLimit the maximum number of players allowed for the created game
     * @param user the user that should be the creator of the game
     * @return the [GameEntity] of the newly created game
     */
    @Transactional
    fun create(playerLimit: Int, user: UserEntity): GameEntity {
        return gamePersistence.save(
            GameEntity(
                creator = user,
                maxNumberOfPlayers = playerLimit
            ).apply {
                // add the creator as player to the game
                players.add(
                    PlayerEntity(user = user, game = this, seat = 0)
                )
            }
        )
    }

    /**
     * Start the game with [id] as [user].
     *
     * @param id the [UUID] of the game to start
     * @param user the user that wants to start the game, must be the creator
     * @return the [GameEntity] of the started game
     */
    @Transactional
    fun start(id: UUID, user: UserEntity): GameEntity {
        val game = gamePersistence.load(id)

        val mfp = ModelFactoryProvider()

        GameStartModel(game, mfp).start(
            mfp.user.create(user)
        )

        return game
    }
}