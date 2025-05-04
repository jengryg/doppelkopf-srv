package game.doppelkopf.domain.game

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.adapter.persistence.model.hand.HandPersistence
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerPersistence
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.ports.commands.GameCommandAddUserAsPlayer
import game.doppelkopf.domain.game.ports.commands.GameCommandDealNewRound
import game.doppelkopf.domain.game.ports.commands.GameCommandStartPlaying
import game.doppelkopf.domain.game.ports.commands.IGameCommand
import game.doppelkopf.domain.game.service.GameDealModel
import game.doppelkopf.domain.game.service.GameJoinModel
import game.doppelkopf.domain.game.service.GameStartModel
import game.doppelkopf.domain.user.model.IUserModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class GameEngine(
    private val gamePersistence: GamePersistence,
    private val roundPersistence: RoundPersistence,
    private val handPersistence: HandPersistence,
    private val playerPersistence: PlayerPersistence,
) {
    @Transactional
    fun execute(command: GameCommandStartPlaying): GameEntity {
        val resources = prepareResources(command)

        GameStartModel(
            entity = resources.game,
            factoryProvider = resources.mfp
        ).start(user = resources.user)

        return resources.game
    }

    @Transactional
    fun execute(command: GameCommandDealNewRound): RoundEntity {
        val resources = prepareResources(command)

        val (round, hands) = GameDealModel(
            entity = resources.game,
            factoryProvider = resources.mfp
        ).deal(user = resources.user)

        return roundPersistence.save(round.entity).also {
            handPersistence.saveAll(hands.map { it.entity })
        }
    }

    @Transactional
    fun execute(command: GameCommandAddUserAsPlayer): PlayerEntity {
        val resources = prepareResources(command)

        val player = GameJoinModel(
            entity = resources.game,
            factoryProvider = resources.mfp
        ).join(user = resources.user, seat = command.seat)

        return playerPersistence.save(player.entity)
    }

    private fun prepareResources(command: IGameCommand): GameCommandResources {
        val mfp = ModelFactoryProvider()

        val game = gamePersistence.load(command.gameId)
        val user = mfp.user.create(command.user.entity)

        return GameCommandResources(
            user = user,
            game = game,
            mfp = mfp
        )
    }

    private inner class GameCommandResources(
        val user: IUserModel,
        val game: GameEntity,
        val mfp: ModelFactoryProvider
    )
}