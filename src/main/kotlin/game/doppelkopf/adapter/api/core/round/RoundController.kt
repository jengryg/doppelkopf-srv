package game.doppelkopf.adapter.api.core.round

import game.doppelkopf.adapter.api.core.round.dto.RoundInfoDto
import game.doppelkopf.adapter.api.core.round.dto.RoundOperationDto
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.domain.game.GameEngine
import game.doppelkopf.domain.game.ports.commands.GameCommandDealNewRound
import game.doppelkopf.domain.round.RoundEngine
import game.doppelkopf.domain.round.enums.RoundOperation
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluateBids
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluateDeclarations
import game.doppelkopf.domain.round.ports.commands.RoundCommandResolveMarriage
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
    private val roundPersistence: RoundPersistence,
    private val gameEngine: GameEngine,
    private val roundEngine: RoundEngine
) {
    @Operation(
        summary = "Obtain all rounds of a specific game.",
        description = "Gets a list containing information about all rounds of the specified game."
    )
    @GetMapping("/games/{gameId}/rounds")
    fun list(@PathVariable gameId: UUID): ResponseEntity<List<RoundInfoDto>> {
        return ResponseEntity.ok(
            roundPersistence.listForGame(gameId).map { RoundInfoDto(it) }
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
        return gameEngine.execute(
            command = GameCommandDealNewRound(
                user = userDetails,
                gameId = gameId
            )
        ).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/rounds/{id}").build(it.id)
            ).body(RoundInfoDto(it))
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
            RoundInfoDto(roundPersistence.load(id))
        )
    }

    @Operation(
        summary = "Perform operation on the round.",
        description = "Performs operations on the round with specified id."
    )
    @PatchMapping("/rounds/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: RoundOperationDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<RoundInfoDto> {
        return when (operation.op) {
            RoundOperation.DECLARE_EVALUATION -> roundEngine.execute(
                command = RoundCommandEvaluateDeclarations(
                    user = userDetails,
                    roundId = id,
                )
            )

            RoundOperation.BID_EVALUATION -> roundEngine.execute(
                command = RoundCommandEvaluateBids(
                    user = userDetails,
                    roundId = id,
                )
            )

            RoundOperation.MARRIAGE_RESOLVER -> roundEngine.execute(
                command = RoundCommandResolveMarriage(
                    user = userDetails,
                    roundId = id,
                )
            )
        }.let {
            ResponseEntity.ok(RoundInfoDto(it))
        }
    }
}