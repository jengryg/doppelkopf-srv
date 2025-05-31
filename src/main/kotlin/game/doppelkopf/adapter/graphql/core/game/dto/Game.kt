package game.doppelkopf.adapter.graphql.core.game.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.adapter.graphql.core.player.dto.Player
import game.doppelkopf.adapter.graphql.core.round.dto.Round
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.game.enums.GameState
import java.util.*

data class Game(
    val id: UUID,
    val playerLimit: Int,
    val state: GameState,
    private val _cu: Lazy<CreatedUpdated>,
    private val _se: Lazy<StartedEnded>,
    private val _players: Lazy<List<Player>>,
    private val _rounds: Lazy<List<Round>>,
    private val _currentRound: Lazy<Round?>,
) {
    val cu: CreatedUpdated by _cu
    val se: StartedEnded by _se
    val players: List<Player> by _players
    val rounds: List<Round> by _rounds
    val currentRound: Round? by _currentRound

    constructor(entity: GameEntity, currentUser: UserEntity?) : this(
        id = entity.id,
        playerLimit = entity.maxNumberOfPlayers,
        state = entity.state,
        _cu = lazy { CreatedUpdated(entity) },
        _se = lazy { StartedEnded(entity) },
        _players = lazy { entity.players.map { Player(it, currentUser) } },
        _rounds = lazy { entity.rounds.map { Round(it, currentUser) } },
        _currentRound = lazy {
            entity.rounds.maxByOrNull { it.number }?.let { Round(it, currentUser) }
        }
    )
}
