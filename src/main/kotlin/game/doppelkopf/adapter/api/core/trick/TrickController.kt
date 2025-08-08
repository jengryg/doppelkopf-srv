package game.doppelkopf.adapter.api.core.trick

import game.doppelkopf.adapter.api.core.trick.dto.TrickInfoResponse
import game.doppelkopf.adapter.api.core.trick.dto.TrickOperationRequest
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.domain.trick.TrickActionOrchestrator
import game.doppelkopf.domain.trick.enums.TrickOperation
import game.doppelkopf.domain.trick.ports.actions.TrickActionEvaluate
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1")
class TrickController(
    private val trickPersistence: TrickPersistence,
    private val trickActionOrchestrator: TrickActionOrchestrator
) {
    @Operation(
        summary = "Obtain all tricks of a specific round.",
        description = "Gets a list containing information about all tricks of the specified game."
    )
    @GetMapping("/rounds/{roundId}/tricks")
    fun list(@PathVariable roundId: UUID): ResponseEntity<List<TrickInfoResponse>> {
        return ResponseEntity.ok(
            trickPersistence.listForRound(roundId).map { TrickInfoResponse(it) }
        )
    }

    @Operation(
        summary = "Show trick information.",
        description = "Gets the information about the trick with the specified id."
    )
    @GetMapping("/tricks/{trickId}")
    fun show(
        @PathVariable trickId: UUID
    ): ResponseEntity<TrickInfoResponse> {
        return ResponseEntity.ok(
            TrickInfoResponse(trickPersistence.load(trickId))
        )
    }

    @Operation(
        summary = "Perform operation on the trick.",
        description = "Performs operations on the trick with specified id."
    )
    @PatchMapping("/tricks/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: TrickOperationRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TrickInfoResponse> {
        return when (operation.op) {
            TrickOperation.TRICK_EVALUATION -> trickActionOrchestrator.execute(
                action = TrickActionEvaluate(
                    user = userDetails,
                    trickId = id,
                )
            )
        }.let {
            ResponseEntity.ok(TrickInfoResponse(it))
        }
    }
}