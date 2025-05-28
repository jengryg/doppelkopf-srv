package game.doppelkopf.adapter.graphql.core.round

import game.doppelkopf.adapter.graphql.core.round.dto.Round
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.domain.game.GameActionOrchestrator
import game.doppelkopf.domain.game.ports.actions.GameActionDealNewRound
import game.doppelkopf.security.UserDetails
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class RoundGraphQLController(
    private val roundPersistence: RoundPersistence,
    private val gameActionOrchestrator: GameActionOrchestrator,
) {
    @QueryMapping
    fun round(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Round {
        return Round(roundPersistence.load(id), userDetails.entity)
    }

    @MutationMapping
    fun createRound(
        @Argument gameId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Round {
        return gameActionOrchestrator.execute(
            action = GameActionDealNewRound(
                user = userDetails,
                gameId = gameId
            )
        ).let {
            Round(it, userDetails.entity)
        }
    }
}