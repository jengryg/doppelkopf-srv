package game.doppelkopf.api.core.player

import game.doppelkopf.api.core.player.dto.PlayerCreateDto
import game.doppelkopf.api.core.player.dto.PlayerInfoDto
import game.doppelkopf.core.PlayerFacade
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

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
        val player = playerFacade.create(
            gameId = gameId,
            seat = playerCreateDto.seat,
            user = userDetails.entity
        )

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