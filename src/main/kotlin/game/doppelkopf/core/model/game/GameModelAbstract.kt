package game.doppelkopf.core.model.game

import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.utils.Quadruple

/**
 * [GameModelAbstract] provides automatic delegation of [IGameProperties] and demands implementation of relevant related
 * models.
 */
abstract class GameModelAbstract(
    entity: GameEntity
) : IGameProperties by entity, ModelAbstract<GameEntity>(entity) {
    val creator get() = UserModel.create(entity.creator)

    val players
        get() = entity.players.associate {
            UserModel.create(it.user) to PlayerModel.create(it)
        }

    val rounds
        get() = entity.rounds.associate {
            it.number to RoundModel.create(it)
        }

    /**
     * Adds [p] to the [players] of this game.
     */
    fun addPlayer(p: PlayerModel) {
        entity.players.add(p.entity)
    }

    /**
     * Adds [r] to the [rounds] of this game.
     */
    fun addRound(r: RoundModel) {
        entity.rounds.add(r.entity)
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

        val sortedPlayers = players.values.sortedBy { it.seat }
        val startIndex = sortedPlayers.indexOf(p).takeIf { it != -1 }
            ?: throw GameFailedException("Could not determine the position of $p.")

        // 0 is the given player, we start with the player sitting directly behind, i.e. with position 1
        val players = (1..4).map { position ->
            sortedPlayers[(startIndex + position) % sortedPlayers.size]
        }

        return Quadruple(players[0], players[1], players[2], players[3])
    }
}