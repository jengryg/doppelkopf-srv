package game.doppelkopf.core

import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerPersistence
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.domain.game.handler.GameJoinModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlayerFacade(
    private val playerPersistence: PlayerPersistence,
    private val gamePersistence: GamePersistence
) {
    /**
     * A player is crated when [user] joins the game at the given [seat] position.
     */
    @Transactional
    fun create(gameId: UUID, seat: Int, user: UserEntity): PlayerEntity {
        val game = gamePersistence.load(gameId)

        val mfp = ModelFactoryProvider()

        return GameJoinModel(game, mfp).join(
            mfp.user.create(user), seat
        ).let { playerPersistence.save(it.entity) }
    }
}