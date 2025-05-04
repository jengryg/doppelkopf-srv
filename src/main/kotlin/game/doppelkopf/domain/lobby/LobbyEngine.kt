package game.doppelkopf.domain.lobby

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.lobby.ports.commands.ILobbyCommand
import game.doppelkopf.domain.lobby.ports.commands.LobbyCommandCreateNewGame
import game.doppelkopf.domain.user.model.IUserModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class LobbyEngine(private val gamePersistence: GamePersistence) {
    @Transactional
    fun execute(command: LobbyCommandCreateNewGame): GameEntity {
        val resources = prepareResources(command)

        val game = GameEntity(
            creator = resources.user.entity,
            maxNumberOfPlayers = command.playerLimit
        ).apply {
            players.add(PlayerEntity(user = command.user.entity, game = this, seat = 0))
        }

        return gamePersistence.save(game)
    }

    private fun prepareResources(command: ILobbyCommand): LobbyCommandResources {
        val mfp = ModelFactoryProvider()

        val user = mfp.user.create(command.user.entity)

        return LobbyCommandResources(
            user = user,
            mfp = mfp,
        )
    }

    private inner class LobbyCommandResources(
        val user: IUserModel,
        val mfp: ModelFactoryProvider
    )
}