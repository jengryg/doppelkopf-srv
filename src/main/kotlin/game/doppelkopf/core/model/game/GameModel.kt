package game.doppelkopf.core.model.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.utils.Quadruple
import java.time.Instant

class GameModel(
    entity: GameEntity
) : GameModelAbstract(entity) {
    /**
     * Start the game by marking the [started] time and transition the [state] of the game to
     * [GameState.WAITING_FOR_DEAL]. Choose one of the [players] randomly as first dealer of the game.
     */
    fun start() {
        // the first dealer is decided randomly
        players.forEach { it.dealer = false }
        // TODO: seeded randomness for seed based games
        players.random().dealer = true

        started = Instant.now()
        state = GameState.WAITING_FOR_DEAL
    }

    /**
     * Determines the 4 players in order that sit behind the given [p].
     * Order is calculated circular in ascending order of seat numbers starting from the seat number of the [p].
     *
     * Note: If a game has only 4 players, the last active player is the returned [Quadruple] is [p].
     *
     * @return the [Quadruple] of the players behind [p]
     */
    fun getFourPlayersBehind(p: PlayerModel): Quadruple<PlayerModel> {
        if (players.size < 4) {
            throw GameFailedException("Can not determine 4 players when game has only ${players.size} players.")
        }

        val sortedPlayers = players.sortedBy { it.seat }
        val startIndex = sortedPlayers.indexOf(p).takeIf { it != -1 }
            ?: throw GameFailedException("Could not determine the position of $p.")

        // 0 is the given player, we start with the player sitting directly behind, i.e. with position 1
        val players = (1..4).map { position ->
            sortedPlayers[(startIndex + position) % sortedPlayers.size]
        }

        return Quadruple(players[0], players[1], players[2], players[3])
    }
}