package game.doppelkopf.api.game

import game.doppelkopf.api.game.dto.PlayerCreateDto
import game.doppelkopf.api.game.dto.PlayerInfoDto
import game.doppelkopf.core.handler.PlayerFacade
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
    private val playerFacade: PlayerFacade
) {
    @Operation(
        summary = "Obtain all players of the specified game.",
        description = "Gets a list containing information about all players of the specified game."
    )
    @GetMapping("/games/{gameId}/players")
    fun list(
        @PathVariable gameId: UUID,
    ): ResponseEntity<List<PlayerInfoDto>> {
        return ResponseEntity.ok(
            playerFacade.list(gameId).map { PlayerInfoDto(it) }
        )
    }

    @Operation(
        summary = "Create a player by joining a game.",
        description = "Join the game at the seat position specified by the provided data."
    )
    @PostMapping("/games/{gameId}/players")
    fun create(
        @PathVariable gameId: UUID,
        @RequestBody @Valid playerCreateDto: PlayerCreateDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<PlayerInfoDto> {
        val player = playerFacade.create(gameId, playerCreateDto, userDetails.entity)

        return PlayerInfoDto(player).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/players/{id}").build(it.id)
            ).body(it)
        }
    }

    @Operation(
        summary = "Show player information.",
        description = "Gets the information about the player with the specified id."
    )
    @GetMapping("/players/{id}")
    fun show(
        @PathVariable id: UUID
    ): ResponseEntity<PlayerInfoDto> {
        return ResponseEntity.ok(
            PlayerInfoDto(playerFacade.load(id))
        )
    }
}