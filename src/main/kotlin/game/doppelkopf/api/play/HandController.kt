package game.doppelkopf.api.play

import game.doppelkopf.api.play.dto.BidCreateDto
import game.doppelkopf.api.play.dto.DeclarationCreateDto
import game.doppelkopf.api.play.dto.HandForPlayerDto
import game.doppelkopf.api.play.dto.HandPublicInfoDto
import game.doppelkopf.core.handler.HandFacade
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
    private val handFacade: HandFacade
) {
    @Operation(
        summary = "Show general hand information about all hands of the round.",
        description = "Gets general (public) information about the hands of this round."
    )
    @GetMapping("/rounds/{roundId}/hands")
    fun list(
        @PathVariable roundId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<List<HandPublicInfoDto>> {
        return ResponseEntity.ok(
            handFacade.list(roundId).map { HandPublicInfoDto(it) }
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
    ): ResponseEntity<HandForPlayerDto> {
        return ResponseEntity.ok(
            HandForPlayerDto(handFacade.load(handId, userDetails.entity))
        )
    }

    @Operation(
        summary = "Add a declaration to the hand.",
        description = "The declaration must be made exactly one time per hand. Can only be accessed by hand owner."
    )
    @PostMapping("/hands/{handId}/declarations")
    fun declare(
        @PathVariable handId: UUID,
        @RequestBody declarationCreateDto: DeclarationCreateDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<HandForPlayerDto> {
        return HandForPlayerDto(handFacade.declare(handId, declarationCreateDto.declaration, userDetails.entity)).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/hands/{id}").build(it.id)
            ).body(it)
        }
    }

    @Operation(
        summary = "Add a bid to the hand.",
        description = "The bid must be made if and only if the declaration was as RESERVATION on the hand. Can only be accessed by hand owner."
    )
    @PostMapping("/hands/{handId}/bids")
    fun bid(
        @PathVariable handId: UUID,
        @RequestBody bidCreateDto: BidCreateDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<HandForPlayerDto> {
        return HandForPlayerDto(handFacade.bid(handId, bidCreateDto.bid, userDetails.entity)).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/hands/{id}").build(it.id)
            ).body(it)
        }
    }
}