package game.doppelkopf.adapter.graphql.core.turn

import game.doppelkopf.adapter.graphql.core.round.dto.PlayCardInput
import game.doppelkopf.adapter.graphql.core.turn.dto.Turn
import game.doppelkopf.adapter.persistence.model.turn.TurnPersistence
import game.doppelkopf.domain.round.RoundActionOrchestrator
import game.doppelkopf.domain.round.ports.actions.RoundActionPlayCard
import game.doppelkopf.security.UserDetails
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class TurnGraphQLController(
    private val turnPersistence: TurnPersistence,
    private val roundActionOrchestrator: RoundActionOrchestrator,
) {
    @QueryMapping
    fun turn(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Turn {
        return Turn(turnPersistence.load(id), userDetails.entity)
    }

    @MutationMapping
    fun playCard(
        @Argument playCardInput: PlayCardInput,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Turn {
        return roundActionOrchestrator.execute(
            action = RoundActionPlayCard(
                user = userDetails,
                roundId = playCardInput.roundId,
                encodedCard = playCardInput.card
            )
        ).let {
            Turn(it, userDetails.entity)
        }
    }
}