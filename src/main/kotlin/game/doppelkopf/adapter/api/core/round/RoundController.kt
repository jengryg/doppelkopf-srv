package game.doppelkopf.adapter.api.core.round

import game.doppelkopf.adapter.api.core.round.dto.RoundInfoResponse
import game.doppelkopf.adapter.api.core.round.dto.RoundOperationRequest
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.domain.game.GameActionOrchestrator
import game.doppelkopf.domain.game.ports.actions.GameActionDealNewRound
import game.doppelkopf.domain.round.RoundActionOrchestrator
import game.doppelkopf.domain.round.enums.RoundOperation
import game.doppelkopf.domain.round.ports.actions.RoundActionEvaluate
import game.doppelkopf.domain.round.ports.actions.RoundActionEvaluateBids
import game.doppelkopf.domain.round.ports.actions.RoundActionEvaluateDeclarations
import game.doppelkopf.domain.round.ports.actions.RoundActionResolveMarriage
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
    private val gameActionOrchestrator: GameActionOrchestrator,
    private val roundActionOrchestrator: RoundActionOrchestrator
) {
    @Operation(
        summary = "Obtain all rounds of a specific game.",
        description = "Gets a list containing information about all rounds of the specified game."
    )
    @GetMapping("/games/{gameId}/rounds")
    fun list(@PathVariable gameId: UUID): ResponseEntity<List<RoundInfoResponse>> {
        return ResponseEntity.ok(
            roundPersistence.listForGame(gameId).map { RoundInfoResponse(it) }
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
    ): ResponseEntity<RoundInfoResponse> {
        return gameActionOrchestrator.execute(
            action = GameActionDealNewRound(
                user = userDetails,
                gameId = gameId
            )
        ).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/rounds/{id}").build(it.id)
            ).body(RoundInfoResponse(it))
        }
    }

    @Operation(
        summary = "Show round information.",
        description = "Gets the information about the round with the specified id."
    )
    @GetMapping("/rounds/{id}")
    fun show(
        @PathVariable id: UUID
    ): ResponseEntity<RoundInfoResponse> {
        return ResponseEntity.ok(
            RoundInfoResponse(roundPersistence.load(id))
        )
    }

    @Operation(
        summary = "Perform operation on the round.",
        description = "Performs operations on the round with specified id."
    )
    @PatchMapping("/rounds/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: RoundOperationRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<RoundInfoResponse> {
        return when (operation.op) {
            RoundOperation.DECLARE_EVALUATION -> roundActionOrchestrator.execute(
                action = RoundActionEvaluateDeclarations(
                    user = userDetails,
                    roundId = id,
                )
            )

            RoundOperation.BID_EVALUATION -> roundActionOrchestrator.execute(
                action = RoundActionEvaluateBids(
                    user = userDetails,
                    roundId = id,
                )
            )

            RoundOperation.MARRIAGE_RESOLVER -> roundActionOrchestrator.execute(
                action = RoundActionResolveMarriage(
                    user = userDetails,
                    roundId = id,
                )
            )

            RoundOperation.RESULT_EVALUATION -> roundActionOrchestrator.execute(
                action = RoundActionEvaluate(
                    user = userDetails,
                    roundId = id,
                )
            )
        }.let {
            ResponseEntity.ok(RoundInfoResponse(it))
        }
    }
}