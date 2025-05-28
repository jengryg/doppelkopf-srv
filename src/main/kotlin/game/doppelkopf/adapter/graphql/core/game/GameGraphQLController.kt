package game.doppelkopf.adapter.graphql.core.game

import game.doppelkopf.adapter.graphql.core.game.dto.CreateGameInput
import game.doppelkopf.adapter.graphql.core.game.dto.Game
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.domain.game.GameActionOrchestrator
import game.doppelkopf.domain.game.ports.actions.GameActionStartPlaying
import game.doppelkopf.domain.lobby.LobbyActionOrchestrator
import game.doppelkopf.domain.lobby.ports.actions.LobbyActionCreateNewGame
import game.doppelkopf.security.UserDetails
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class GameGraphQLController(
    private val gamePersistence: GamePersistence,
    private val lobbyActionOrchestrator: LobbyActionOrchestrator,
    private val gameActionOrchestrator: GameActionOrchestrator,
) {
    @QueryMapping
    fun games(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<Game> {
        // TODO: pagination
        return gamePersistence.list().map {
            Game(it, userDetails.entity)
        }
    }

    @QueryMapping
    fun game(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Game {
        return Game(gamePersistence.load(id), userDetails.entity)
    }

    @MutationMapping
    fun createGame(
        @Argument @Valid createGameInput: CreateGameInput,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Game {
        return lobbyActionOrchestrator.execute(
            action = LobbyActionCreateNewGame(
                user = userDetails,
                playerLimit = createGameInput.playerLimit
            )
        ).let {
            Game(it, userDetails.entity)
        }
    }

    @MutationMapping
    fun startGame(
        @Argument gameId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Game {
        return gameActionOrchestrator.execute(
            action = GameActionStartPlaying(
                user = userDetails,
                gameId = gameId
            )
        ).let { Game(it, userDetails.entity) }
    }
}