package game.doppelkopf.domain.game

import game.doppelkopf.adapter.persistence.model.hand.HandPersistence
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerPersistence
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.ports.commands.GameCommandDealNewRound
import game.doppelkopf.domain.game.ports.commands.GameCommandJoinAsPlayer
import game.doppelkopf.domain.game.ports.commands.GameCommandStartPlaying
import game.doppelkopf.domain.game.service.GameDealModel
import game.doppelkopf.domain.game.service.GameJoinModel
import game.doppelkopf.domain.game.service.GameStartModel
import org.springframework.stereotype.Service

@Service
class GameEngine(
    private val roundPersistence: RoundPersistence,
    private val handPersistence: HandPersistence,
    private val playerPersistence: PlayerPersistence,
) {
    fun execute(command: GameCommandStartPlaying) {
        val mfp = ModelFactoryProvider()

        GameStartModel(
            entity = command.game,
            factoryProvider = mfp
        ).start(
            user = mfp.user.create(command.user)
        )
    }

    fun execute(command: GameCommandDealNewRound): RoundEntity {
        val mfp = ModelFactoryProvider()

        val (round, hands) = GameDealModel(
            entity = command.game,
            factoryProvider = mfp
        ).deal(
            user = mfp.user.create(command.user)
        )

        return roundPersistence.save(round.entity).also {
            handPersistence.saveAll(hands.map { it.entity })
        }
    }

    fun execute(command: GameCommandJoinAsPlayer): PlayerEntity {
        val mfp = ModelFactoryProvider()

        val player = GameJoinModel(
            entity = command.game,
            factoryProvider = mfp
        ).join(
            user = mfp.user.create(command.user),
            seat = command.seat
        )

        return playerPersistence.save(player.entity)
    }
}