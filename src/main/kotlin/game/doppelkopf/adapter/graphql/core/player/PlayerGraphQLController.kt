package game.doppelkopf.adapter.graphql.core.player

import game.doppelkopf.adapter.graphql.core.player.dto.JoinGameInput
import game.doppelkopf.adapter.graphql.core.player.dto.Player
import game.doppelkopf.adapter.persistence.model.player.PlayerPersistence
import game.doppelkopf.domain.game.GameActionOrchestrator
import game.doppelkopf.domain.game.ports.actions.GameActionJoinAsPlayer
import game.doppelkopf.security.UserDetails
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class PlayerGraphQLController(
    private val gameActionOrchestrator: GameActionOrchestrator,
    private val playerPersistence: PlayerPersistence
) {
    @QueryMapping
    fun player(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Player {
        return Player(playerPersistence.load(id), userDetails.entity)
    }

    @MutationMapping
    fun joinGame(
        @Argument @Valid joinGameInput: JoinGameInput,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Player {
        return gameActionOrchestrator.execute(
            action = GameActionJoinAsPlayer(
                user = userDetails,
                gameId = joinGameInput.gameId,
                seat = joinGameInput.seat,
            )
        ).let { Player(it, userDetails.entity) }
    }
}