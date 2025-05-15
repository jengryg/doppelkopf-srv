package game.doppelkopf.adapter.api.core.call

import game.doppelkopf.adapter.api.core.call.dto.CallCreateDto
import game.doppelkopf.adapter.api.core.call.dto.CallInfoDto
import game.doppelkopf.adapter.persistence.model.call.CallPersistence
import game.doppelkopf.domain.hand.HandActionOrchestrator
import game.doppelkopf.domain.hand.ports.actions.HandActionCall
import game.doppelkopf.domain.round.RoundActionOrchestrator
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("/v1")
class CallController(
    private val callPersistence: CallPersistence,
    private val handActionOrchestrator: HandActionOrchestrator
) {
    @Operation(
        summary = "Obtain all turns of a specific hand.",
        description = "Gets a list containing information about all calls of the specified hand."
    )
    @GetMapping("/hands/{handId}/calls")
    fun list(
        @PathVariable handId: UUID
    ): ResponseEntity<List<CallInfoDto>> {
        return ResponseEntity.ok(
            callPersistence.listForHand(handId).map { CallInfoDto(it) }
        )
    }

    @Operation(
        summary = "Show hand information.",
        description = "Gets the information about the call with the specified id."
    )
    @GetMapping("/calls/{callId}")
    fun show(
        @PathVariable callId: UUID
    ): ResponseEntity<CallInfoDto> {
        return ResponseEntity.ok(
            CallInfoDto(callPersistence.load(callId))
        )
    }

    @Operation(
        summary = "Create a call.",
        description = "Adds a new call to the given hand."
    )
    @PostMapping("/hands/{handId}/calls")
    fun call(
        @PathVariable handId: UUID,
        @RequestBody callCreateDto: CallCreateDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CallInfoDto> {
        return handActionOrchestrator.execute(
            action = HandActionCall(
                user = userDetails,
                handId = handId,
                callType = callCreateDto.callType
            )
        ).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/calls/{id}").build(it.id)
            ).body(CallInfoDto(it))
        }
    }
}