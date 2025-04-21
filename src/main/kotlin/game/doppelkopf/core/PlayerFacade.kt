package game.doppelkopf.core

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.handler.GameJoinModel
import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.player.PlayerRepository
import game.doppelkopf.persistence.model.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlayerFacade(
    private val gameFacade: GameFacade,
    private val playerRepository: PlayerRepository
) {
    fun list(gameId: UUID): List<PlayerEntity> {
        return gameFacade.load(gameId).players.toList()
    }

    fun load(playerId: UUID): PlayerEntity {
        return playerRepository.findByIdOrNull(playerId)
            ?: throw EntityNotFoundException.forEntity<PlayerEntity>(playerId)
    }

    /**
     * A player is crated when [user] joins the game at the given [seat] position.
     */
    @Transactional
    fun create(gameId: UUID, seat: Int, user: UserEntity): PlayerEntity {
        val game = gameFacade.load(gameId)

        val mfp = ModelFactoryProvider()

        return GameJoinModel(game, mfp).join(
            mfp.user.create(user), seat
        ).let { playerRepository.save(it.entity) }
    }
}