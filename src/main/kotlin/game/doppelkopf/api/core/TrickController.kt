package game.doppelkopf.api.core

import game.doppelkopf.api.core.dto.trick.TrickInfoDto
import game.doppelkopf.core.TrickFacade
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

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
}