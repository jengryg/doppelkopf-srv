package game.doppelkopf.adapter.api.core.player

import game.doppelkopf.adapter.api.core.player.dto.PlayerCreateRequest
import game.doppelkopf.adapter.api.core.player.dto.PlayerInfoResponse
import game.doppelkopf.adapter.persistence.model.player.PlayerPersistence
import game.doppelkopf.domain.game.GameActionOrchestrator
import game.doppelkopf.domain.game.ports.actions.GameActionJoinAsPlayer
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

/**
 * Lets users join a game as player and provides information about the users own players.
 */
@RestController
@RequestMapping("/v1")
class PlayerController(
    private val playerPersistence: PlayerPersistence,
    private val gameActionOrchestrator: GameActionOrchestrator
) {
    @Operation(
        summary = "Obtain all players of the specified game.",
        description = "Gets a list containing information about all players of the specified game."
    )
    @GetMapping("/games/{gameId}/players")
    fun list(
        @PathVariable gameId: UUID,
    ): ResponseEntity<List<PlayerInfoResponse>> {
        return ResponseEntity.ok(
            playerPersistence.listForGame(gameId).map { PlayerInfoResponse(it) }
        )
    }

    @Operation(
        summary = "Create a player by joining a game.",
        description = "Join the game at the seat position specified by the provided data."
    )
    @PostMapping("/games/{gameId}/players")
    fun create(
        @PathVariable gameId: UUID,
        @RequestBody @Valid playerCreateRequest: PlayerCreateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<PlayerInfoResponse> {
        return gameActionOrchestrator.execute(
            action = GameActionJoinAsPlayer(
                userDetails,
                gameId = gameId,
                seat = playerCreateRequest.seat
            )
        ).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/players/{id}").build(it.id)
            ).body(PlayerInfoResponse(it))
        }
    }

    @Operation(
        summary = "Show player information.",
        description = "Gets the information about the player with the specified id."
    )
    @GetMapping("/players/{id}")
    fun show(
        @PathVariable id: UUID
    ): ResponseEntity<PlayerInfoResponse> {
        return ResponseEntity.ok(
            PlayerInfoResponse(playerPersistence.load(id))
        )
    }
}