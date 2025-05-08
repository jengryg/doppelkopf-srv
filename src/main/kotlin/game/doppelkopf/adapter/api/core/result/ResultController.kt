package game.doppelkopf.adapter.api.core.result

import game.doppelkopf.adapter.api.core.result.dto.ResultInfoDto
import game.doppelkopf.adapter.api.core.result.dto.TeamedResultInfoDto
import game.doppelkopf.adapter.persistence.model.result.ResultPersistence
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v1")
class ResultController(
    private val resultPersistence: ResultPersistence
) {
    @Operation(
        summary = "Obtain all results of a specific round.",
        description = "Gets a team tagged map containing information about the results of the specific round. " +
                "If the result is not available, the Teamed contains null for re and ko."
    )
    @GetMapping("/rounds/{roundId}/results")
    fun list(
        @PathVariable roundId: UUID
    ): ResponseEntity<TeamedResultInfoDto> {
        return ResponseEntity.ok(
            TeamedResultInfoDto(resultPersistence.loadForRound(roundId))
        )
    }

    @Operation(
        summary = "Show result information.",
        description = "Gets information about the result with the specified id."
    )
    @GetMapping("/results/{resultId}")
    fun show(
        @PathVariable resultId: UUID,
    ): ResponseEntity<ResultInfoDto> {
        return ResponseEntity.ok(
            ResultInfoDto(resultPersistence.load(resultId))
        )
    }
}