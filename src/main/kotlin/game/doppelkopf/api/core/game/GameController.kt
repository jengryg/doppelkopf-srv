package game.doppelkopf.api.core.game

import game.doppelkopf.api.core.game.dto.GameCreateDto
import game.doppelkopf.api.core.game.dto.GameInfoDto
import game.doppelkopf.api.core.game.dto.GameOperationDto
import game.doppelkopf.core.GameFacade
import game.doppelkopf.core.common.enums.GameOperation
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

/**
 * List, create, join and start the game.
 */
@RestController
@RequestMapping("/v1")
class GameController(
    private val gameFacade: GameFacade
) {
    @Operation(
        summary = "List games.",
        description = "Gets a list containing information about all games.",
    )
    @GetMapping("/games")
    fun list(): ResponseEntity<List<GameInfoDto>> {
        return ResponseEntity.ok(
            gameFacade.list().map { GameInfoDto(it) }
        )
    }

    @Operation(
        summary = "Create a new game.",
        description = "Creates a new game based on the provided data."
    )
    @PostMapping("/games")
    fun create(
        @RequestBody @Valid gameCreateDto: GameCreateDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GameInfoDto> {
        val game = gameFacade.create(
            playerLimit = gameCreateDto.playerLimit,
            user = userDetails.entity
        )

        return GameInfoDto(game).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/games/{id}").build(it.id)
            ).body(it)
        }
    }

    @Operation(
        summary = "Show game.",
        description = "Gets the information about the game with the specified id.",
    )
    @GetMapping("/games/{id}")
    fun show(@PathVariable id: UUID): ResponseEntity<GameInfoDto> {
        return ResponseEntity.ok(
            GameInfoDto(gameFacade.load(id))
        )
    }

    @Operation(
        summary = "Perform operation on the game.",
        description = "Perform operations on the game with the specified id."
    )
    @PatchMapping("/games/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: GameOperationDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GameInfoDto> {
        return when (operation.op) {
            GameOperation.START -> ResponseEntity.ok(GameInfoDto(gameFacade.start(id, userDetails.entity)))
        }
    }
}