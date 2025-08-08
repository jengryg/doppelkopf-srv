package game.doppelkopf.adapter.api.core.call

import game.doppelkopf.adapter.api.core.call.dto.CallCreateRequest
import game.doppelkopf.adapter.api.core.call.dto.CallInfoResponse
import game.doppelkopf.adapter.persistence.model.call.CallPersistence
import game.doppelkopf.domain.hand.HandActionOrchestrator
import game.doppelkopf.domain.hand.ports.actions.HandActionCall
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
    ): ResponseEntity<List<CallInfoResponse>> {
        return ResponseEntity.ok(
            callPersistence.listForHand(handId).map { CallInfoResponse(it) }
        )
    }

    @Operation(
        summary = "Show hand information.",
        description = "Gets the information about the call with the specified id."
    )
    @GetMapping("/calls/{callId}")
    fun show(
        @PathVariable callId: UUID
    ): ResponseEntity<CallInfoResponse> {
        return ResponseEntity.ok(
            CallInfoResponse(callPersistence.load(callId))
        )
    }

    @Operation(
        summary = "Create a call.",
        description = "Adds a new call to the given hand."
    )
    @PostMapping("/hands/{handId}/calls")
    fun call(
        @PathVariable handId: UUID,
        @RequestBody callCreateRequest: CallCreateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CallInfoResponse> {
        return handActionOrchestrator.execute(
            action = HandActionCall(
                user = userDetails,
                handId = handId,
                callType = callCreateRequest.callType
            )
        ).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/calls/{id}").build(it.id)
            ).body(CallInfoResponse(it))
        }
    }
}