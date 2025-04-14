package game.doppelkopf.core

import game.doppelkopf.api.game.dto.GameCreateDto
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.game.GameRepository
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class GameFacade(
    private val gameRepository: GameRepository
) {
    /**
     * @return a list of all [GameEntity] in the database.
     *
     * TODO: implement pagination and limiting
     */
    fun list(): List<GameEntity> {
        return gameRepository.findAll()
    }

    /**
     * @param id the [UUID] of the game to get
     * @return the [GameEntity] with [id]
     * @throws EntityNotFoundException if the [GameEntity] with [id] can not be found
     */
    fun load(id: UUID): GameEntity {
        return gameRepository.findByIdOrNull(id) ?: throw EntityNotFoundException.forEntity<GameEntity>(id)
    }

    /**
     * Create a new game from the given [gameCreateDto] that is owned by [user].
     *
     * @param gameCreateDto the information to use for the new game
     * @param user the user that should be the creator of the game
     * @return the [GameEntity] of the newly created game
     */
    @Transactional
    fun create(gameCreateDto: GameCreateDto, user: UserEntity): GameEntity {
        return gameRepository.save(
            GameEntity(
                creator = user,
                maxNumberOfPlayers = gameCreateDto.playerLimit
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
        val game = load(id)

        GameModel.create(entity = game).start(user = UserModel.create(user))

        return game
    }
}