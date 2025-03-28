package game.doppelkopf.api.game

import game.doppelkopf.api.game.dto.PlayerCreateDto
import game.doppelkopf.api.game.dto.PlayerInfoDto
import game.doppelkopf.core.game.PlayerService
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
@RequestMapping("/v1/players")
class PlayerController(
    private val playerService: PlayerService
) {
    @Operation(
        summary = "Obtain all players.",
        description = "Gets a list containing information about all players."
    )
    @GetMapping("")
    fun list(): ResponseEntity<List<PlayerInfoDto>> {
        return ResponseEntity.ok(
            playerService.list().map { PlayerInfoDto(it) }
        )
    }

    @Operation(
        summary = "Create a player by joining a game.",
        description = "Join the game at the seat position specified by the provided data."
    )
    @PostMapping("")
    fun create(
        @RequestBody @Valid playerCreateDto: PlayerCreateDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<PlayerInfoDto> {
        return PlayerInfoDto(playerService.create(playerCreateDto, userDetails.user)).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/players/{id}").build(it.id)
            ).body(it)
        }
    }

    @Operation(
        summary = "Show player information.",
        description = "Gets the information about the player with the specified id."
    )
    @GetMapping("/{id}")
    fun show(
        @PathVariable id: UUID
    ): ResponseEntity<PlayerInfoDto> {
        return ResponseEntity.ok(
            PlayerInfoDto(playerService.load(id))
        )
    }
}