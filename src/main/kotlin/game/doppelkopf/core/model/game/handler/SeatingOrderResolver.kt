package game.doppelkopf.core.model.game.handler

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.GameModelAbstract
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.utils.Quadruple

class SeatingOrderResolver(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : GameModelAbstract(entity, factoryProvider) {
    /**
     * Determine the 4 players in order that sit behind the given [behind].
     * Order is calculated circular in ascending order of seat numbers starting from the seat number of the given
     * [behind].
     *
     * Note: If a game has only 4 players, the last active player in the returned [Quadruple] is [behind].
     *
     * @return the [Quadruple] of the 4 players sitting behind [behind]
     */
    fun getFourPlayersBehind(behind: IPlayerModel): Quadruple<IPlayerModel> {
        if (players.size < 4) {
            throw GameFailedException(
                "Can not determine 4 players when game has only ${players.size} players.",
                entity.id
            )
        }

        val sortedPlayers = players.values.sortedBy { it.seat }

        val startIndex = sortedPlayers.indexOf(behind).takeIf { it != -1 }
            ?: throw GameFailedException(
                "Could not determine the position of given player $behind.",
                entity.id
            )

        // 0 is the given player, we start with the player sitting directly behind, i.e. with position 1
        val players = (1..4).map { position ->
            sortedPlayers[(startIndex + position) % sortedPlayers.size]
        }

        return Quadruple(players[0], players[1], players[2], players[3])
    }
}