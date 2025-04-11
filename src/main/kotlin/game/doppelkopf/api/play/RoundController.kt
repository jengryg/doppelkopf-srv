package game.doppelkopf.api.play

import game.doppelkopf.api.play.dto.RoundInfoDto
import game.doppelkopf.api.play.dto.RoundOperationDto
import game.doppelkopf.core.handler.RoundFacade
import game.doppelkopf.core.common.enums.RoundOperation
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

/**
 * Lets players start and end rounds in a game and provides information about the rounds.
 */
@RestController
@RequestMapping("/v1")
class RoundController(
    private val roundFacade: RoundFacade
) {
    @Operation(
        summary = "Obtain all rounds of a specific game.",
        description = "Gets a list containing information about all rounds of the specified game."
    )
    @GetMapping("/games/{gameId}/rounds")
    fun list(@PathVariable gameId: UUID): ResponseEntity<List<RoundInfoDto>> {
        return ResponseEntity.ok(
            roundFacade.list(gameId).map { RoundInfoDto(it) }
        )
    }

    @Operation(
        summary = "Create a round.",
        description = "Start a new round in a specific game by dealing the cards to the players."
    )
    @PostMapping("/games/{gameId}/rounds")
    fun deal(
        @PathVariable gameId: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<RoundInfoDto> {
        return RoundInfoDto(roundFacade.create(gameId, userDetails.user)).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/rounds/{id}").build(it.id)
            ).body(it)
        }
    }

    @Operation(
        summary = "Show round information.",
        description = "Gets the information about the round with the specified id."
    )
    @GetMapping("/rounds/{id}")
    fun show(
        @PathVariable id: UUID
    ): ResponseEntity<RoundInfoDto> {
        return ResponseEntity.ok(
            RoundInfoDto(roundFacade.load(id))
        )
    }

    @Operation(
        summary = "Perform operation on the round.",
        description = "Performs operations on the round with specified id."
    )
    @PatchMapping("/rounds/{id}")
    fun path(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: RoundOperationDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<RoundInfoDto> {
        return when (operation.op) {
            RoundOperation.DECLARE_EVALUATION -> ResponseEntity.ok(RoundInfoDto(roundFacade.evaluateDeclarations(id)))
            RoundOperation.BID_EVALUATION -> ResponseEntity.ok(RoundInfoDto(roundFacade.evaluateBids(id)))
        }
    }
}