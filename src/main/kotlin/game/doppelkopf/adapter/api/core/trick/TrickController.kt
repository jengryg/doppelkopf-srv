package game.doppelkopf.adapter.api.core.trick

import game.doppelkopf.adapter.api.core.trick.dto.TrickInfoDto
import game.doppelkopf.adapter.api.core.trick.dto.TrickOperationDto
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.domain.trick.TrickEngine
import game.doppelkopf.domain.trick.enums.TrickOperation
import game.doppelkopf.domain.trick.ports.commands.TrickCommandEvaluate
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
    private val trickEngine: TrickEngine
) {
    @Operation(
        summary = "Obtain all tricks of a specific round.",
        description = "Gets a list containing information about all tricks of the specified game."
    )
    @GetMapping("/rounds/{roundId}/tricks")
    fun list(@PathVariable roundId: UUID): ResponseEntity<List<TrickInfoDto>> {
        return ResponseEntity.ok(
            trickPersistence.listForRound(roundId).map { TrickInfoDto(it) }
        )
    }

    @Operation(
        summary = "Show trick information.",
        description = "Gets the information about the trick with the specified id."
    )
    @GetMapping("/tricks/{trickId}")
    fun show(
        @PathVariable trickId: UUID
    ): ResponseEntity<TrickInfoDto> {
        return ResponseEntity.ok(
            TrickInfoDto(trickPersistence.load(trickId))
        )
    }

    @Operation(
        summary = "Perform operation on the trick.",
        description = "Performs operations on the trick with specified id."
    )
    @PatchMapping("/tricks/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: TrickOperationDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TrickInfoDto> {
        return when (operation.op) {
            TrickOperation.TRICK_EVALUATION -> trickEngine.execute(
                command = TrickCommandEvaluate(
                    user = userDetails,
                    trickId = id,
                )
            )
        }.let {
            ResponseEntity.ok(TrickInfoDto(it))
        }
    }
}