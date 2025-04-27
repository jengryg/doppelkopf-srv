package game.doppelkopf.api.core

import game.doppelkopf.api.core.dto.turn.CreateTurnDto
import game.doppelkopf.api.core.dto.turn.TurnInfoDto
import game.doppelkopf.core.TurnFacade
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("/v1")
class TurnController(
    val turnFacade: TurnFacade
) {
    @Operation(
        summary = "Obtain all turns of a specific round.",
        description = "Gets a list containing information about all turns of the specified round."
    )
    @GetMapping("/rounds/{roundId}/turns")
    fun list(
        @PathVariable roundId: UUID
    ): ResponseEntity<List<TurnInfoDto>> {
        return ResponseEntity.ok(
            turnFacade.list(roundId).map { TurnInfoDto(it) }
        )
    }

    @Operation(
        summary = "Create a turn on the round by playing a card.",
        description = "Each time a card is played, a turn is created and the related objects are updated accordingly."
    )
    @PostMapping("/rounds/{roundId}/turns")
    fun playCard(
        @PathVariable roundId: UUID,
        @RequestBody createTurnDto: CreateTurnDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TurnInfoDto> {
        return TurnInfoDto(turnFacade.create(roundId, createTurnDto.card, userDetails.entity)).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/turns/{id}").build(it.id)
            ).body(it)
        }
    }

    @Operation(
        summary = "Show turn information.",
        description = "Gets information about the turn with the specified id."
    )
    @GetMapping("/turns/{turnId}")
    fun show(
        @PathVariable turnId: UUID,
    ): ResponseEntity<TurnInfoDto> {
        return ResponseEntity.ok(
            TurnInfoDto(turnFacade.load(turnId))
        )
    }
}