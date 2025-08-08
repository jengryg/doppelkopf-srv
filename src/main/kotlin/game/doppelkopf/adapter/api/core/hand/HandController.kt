package game.doppelkopf.adapter.api.core.hand

import game.doppelkopf.adapter.api.core.hand.dto.BidCreateRequest
import game.doppelkopf.adapter.api.core.hand.dto.DeclarationCreateRequest
import game.doppelkopf.adapter.api.core.hand.dto.HandForPlayerResponse
import game.doppelkopf.adapter.api.core.hand.dto.HandPublicInfoResponse
import game.doppelkopf.adapter.persistence.model.hand.HandPersistence
import game.doppelkopf.domain.hand.HandActionOrchestrator
import game.doppelkopf.domain.hand.ports.actions.HandActionBid
import game.doppelkopf.domain.hand.ports.actions.HandActionDeclare
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("/v1")
class HandController(
    private val handPersistence: HandPersistence,
    private val handActionOrchestrator: HandActionOrchestrator
) {
    @Operation(
        summary = "Show general hand information about all hands of the round.",
        description = "Gets general (public) information about the hands of this round."
    )
    @GetMapping("/rounds/{roundId}/hands")
    fun list(
        @PathVariable roundId: UUID
    ): ResponseEntity<List<HandPublicInfoResponse>> {
        return ResponseEntity.ok(
            handPersistence.listForRound(roundId).map { HandPublicInfoResponse(it) }
        )
    }

    @Operation(
        summary = "Show detailed hand information for owner.",
        description = "Gets detailed hand information. Can only be accessed by hand owner."
    )
    @GetMapping("/hands/{handId}")
    fun show(
        @PathVariable handId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<HandForPlayerResponse> {
        return ResponseEntity.ok(
            HandForPlayerResponse(handPersistence.loadForUser(handId, userDetails.entity))
        )
    }

    @Operation(
        summary = "Add a declaration to the hand.",
        description = "The declaration must be made exactly one time per hand. Can only be accessed by hand owner."
    )
    @PostMapping("/hands/{handId}/declarations")
    fun declare(
        @PathVariable handId: UUID,
        @RequestBody declarationCreateRequest: DeclarationCreateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<HandForPlayerResponse> {
        return handActionOrchestrator.execute(
            action = HandActionDeclare(
                user = userDetails,
                handId = handId,
                declaration = declarationCreateRequest.declaration
            )
        ).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/hands/{id}").build(it.id)
            ).body(HandForPlayerResponse(it))
        }
    }

    @Operation(
        summary = "Add a bid to the hand.",
        description = "The bid must be made if and only if the declaration was as RESERVATION on the hand. Can only be accessed by hand owner."
    )
    @PostMapping("/hands/{handId}/bids")
    fun bid(
        @PathVariable handId: UUID,
        @RequestBody bidCreateRequest: BidCreateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<HandForPlayerResponse> {
        return handActionOrchestrator.execute(
            action = HandActionBid(
                user = userDetails,
                handId = handId,
                bid = bidCreateRequest.bid
            )
        ).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/hands/{id}").build(it.id)
            ).body(HandForPlayerResponse(it))
        }
    }
}