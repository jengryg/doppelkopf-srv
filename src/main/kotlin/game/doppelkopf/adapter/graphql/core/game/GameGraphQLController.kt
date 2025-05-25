package game.doppelkopf.adapter.graphql.core.game

import game.doppelkopf.adapter.graphql.core.game.dto.CreateGameInput
import game.doppelkopf.adapter.graphql.core.game.dto.Game
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.domain.lobby.LobbyActionOrchestrator
import game.doppelkopf.domain.lobby.ports.actions.LobbyActionCreateNewGame
import game.doppelkopf.security.UserDetails
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class GameGraphQLController(
    private val gamePersistence: GamePersistence,
    private val lobbyActionOrchestrator: LobbyActionOrchestrator,
) {
    @QueryMapping
    fun game(@Argument id: String): Game {
        return gamePersistence.load(id = UUID.fromString(id)).let {
            Game(it)
        }
    }

    @MutationMapping
    fun createGame(
        @Argument createGameInput: CreateGameInput,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Game {
        return lobbyActionOrchestrator.execute(
            action = LobbyActionCreateNewGame(
                user = userDetails,
                playerLimit = createGameInput.playerLimit
            )
        ).let {
            Game(it)
        }
    }
}