package game.doppelkopf.adapter.api.core.trick

import game.doppelkopf.adapter.api.core.trick.dto.TrickInfoDto
import game.doppelkopf.adapter.api.core.trick.dto.TrickOperationDto
import game.doppelkopf.core.TrickFacade
import game.doppelkopf.core.common.enums.TrickOperation
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1")
class TrickController(
    private val trickFacade: TrickFacade
) {
    @Operation(
        summary = "Obtain all tricks of a specific round.",
        description = "Gets a list containing information about all tricks of the specified game."
    )
    @GetMapping("/rounds/{roundId}/tricks")
    fun list(@PathVariable roundId: UUID): ResponseEntity<List<TrickInfoDto>> {
        return ResponseEntity.ok(
            trickFacade.list(roundId).map { TrickInfoDto(it) }
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
            TrickInfoDto(trickFacade.load(trickId))
        )
    }

    @Operation(
        summary = "Perform operation on the trick.",
        description = "Performs operations on the trick with specified id."
    )
    @PatchMapping("/tricks/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: TrickOperationDto
    ): ResponseEntity<TrickInfoDto> {
        return when (operation.op) {
            TrickOperation.TRICK_EVALUATION -> ResponseEntity.ok(TrickInfoDto(trickFacade.evaluateTrick(id)))
        }
    }
}