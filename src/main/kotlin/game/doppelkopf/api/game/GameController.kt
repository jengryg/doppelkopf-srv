package game.doppelkopf.api.game

import game.doppelkopf.api.game.dto.GameCreateDto
import game.doppelkopf.api.game.dto.GameInfoDto
import game.doppelkopf.api.game.dto.GameOperationDto
import game.doppelkopf.core.game.GameOperation
import game.doppelkopf.core.game.GameService
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * List, create, join and start the game.
 */
@RestController
@RequestMapping("/v1/games")
class GameController(private val gameService: GameService) {
    @Operation(
        summary = "List games.",
        description = "Gets a list containing information about all games.",
    )
    @GetMapping("")
    fun list(): List<GameInfoDto> {
        return gameService.list().map { GameInfoDto(it) }
    }

    @Operation(
        summary = "Create a new game.",
        description = "Creates a new game based on the provided data."
    )
    @PostMapping("")
    fun create(
        @RequestBody @Valid gameCreateDto: GameCreateDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): GameInfoDto {
        return GameInfoDto(gameService.create(gameCreateDto, userDetails.user))
    }

    @Operation(
        summary = "Show game.",
        description = "Gets the information about the game with the specified id.",
    )
    @GetMapping("/{id}")
    fun show(@PathVariable id: UUID): GameInfoDto {
        return GameInfoDto(gameService.load(id))
    }

    @Operation(
        summary = "Perform operation on the game.",
        description = "Perform operations on the game with the specified id."
    )
    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody @Valid operation: GameOperationDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): GameInfoDto {
        return when (operation.op) {
            GameOperation.START -> GameInfoDto(gameService.start(id, userDetails.user))
        }
    }
}