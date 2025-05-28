package game.doppelkopf.adapter.graphql.core.hand

import game.doppelkopf.adapter.graphql.core.hand.dto.BidInput
import game.doppelkopf.adapter.graphql.core.hand.dto.DeclareInput
import game.doppelkopf.adapter.graphql.core.hand.dto.PrivateHand
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHand
import game.doppelkopf.adapter.persistence.model.hand.HandPersistence
import game.doppelkopf.domain.hand.HandActionOrchestrator
import game.doppelkopf.domain.hand.ports.actions.HandActionBid
import game.doppelkopf.domain.hand.ports.actions.HandActionDeclare
import game.doppelkopf.security.UserDetails
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class HandGraphQLController(
    private val handPersistence: HandPersistence,
    private val handActionOrchestrator: HandActionOrchestrator,
) {
    @QueryMapping
    fun publicHand(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): PublicHand {
        return PublicHand(handPersistence.load(id), userDetails.entity)
    }

    @QueryMapping
    fun privateHand(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): PrivateHand {
        return PrivateHand(handPersistence.loadForUser(id, userDetails.entity), userDetails.entity)
    }

    @MutationMapping
    fun declare(
        @Argument @Valid declareInput: DeclareInput,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): PrivateHand {
        return handActionOrchestrator.execute(
            action = HandActionDeclare(
                user = userDetails,
                handId = declareInput.handId,
                declaration = declareInput.declaration
            )
        ).let {
            PrivateHand(it, userDetails.entity)
        }
    }

    @MutationMapping
    fun bid(
        @Argument @Valid bidInput: BidInput,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): PrivateHand {
        return handActionOrchestrator.execute(
            action = HandActionBid(
                user = userDetails,
                handId = bidInput.handId,
                bid = bidInput.bid
            )
        ).let {
            PrivateHand(it, userDetails.entity)
        }
    }
}