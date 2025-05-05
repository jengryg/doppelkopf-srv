package game.doppelkopf.adapter.api.core.game

import game.doppelkopf.adapter.api.core.game.dto.GameCreateDto
import game.doppelkopf.adapter.api.core.game.dto.GameInfoDto
import game.doppelkopf.adapter.api.core.game.dto.GameOperationDto
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.domain.game.GameActionOrchestrator
import game.doppelkopf.domain.game.enums.GameOperation
import game.doppelkopf.domain.game.ports.actions.GameActionStartPlaying
import game.doppelkopf.domain.lobby.LobbyActionOrchestrator
import game.doppelkopf.domain.lobby.ports.actions.LobbyActionCreateNewGame
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

/**
 * List, create, join and start the game.
 */
@RestController
@RequestMapping("/v1")
class GameController(
    private val gamePersistence: GamePersistence,
    private val lobbyActionOrchestrator: LobbyActionOrchestrator,
    private val gameActionOrchestrator: GameActionOrchestrator
) {
    @Operation(
        summary = "List games.",
        description = "Gets a list containing information about all games.",
    )
    @GetMapping("/games")
    fun list(): ResponseEntity<List<GameInfoDto>> {
        return ResponseEntity.ok(
            gamePersistence.list().map { GameInfoDto(it) }
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
        val game = lobbyActionOrchestrator.execute(
            action = LobbyActionCreateNewGame(
                user = userDetails,
                playerLimit = gameCreateDto.playerLimit
            ),
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
            GameInfoDto(gamePersistence.load(id))
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
            GameOperation.START -> gameActionOrchestrator.execute(
                action = GameActionStartPlaying(
                    user = userDetails,
                    gameId = id
                )
            )
        }.let { ResponseEntity.ok(GameInfoDto(it)) }
    }
}