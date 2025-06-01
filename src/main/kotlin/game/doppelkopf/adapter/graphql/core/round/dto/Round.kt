package game.doppelkopf.adapter.graphql.core.round.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.adapter.graphql.core.call.dto.Call
import game.doppelkopf.adapter.graphql.core.game.dto.Game
import game.doppelkopf.adapter.graphql.core.hand.dto.PrivateHand
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHand
import game.doppelkopf.adapter.graphql.core.player.dto.Player
import game.doppelkopf.adapter.graphql.core.trick.dto.Trick
import game.doppelkopf.adapter.graphql.core.turn.dto.Turn
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.round.enums.RoundContractPublic
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.utils.Teamed
import java.util.*

data class Round(
    val id: UUID,
    val number: Int,
    val state: RoundState,
    val contract: RoundContractPublic,
    private val _cu: Lazy<CreatedUpdated>,
    private val _se: Lazy<StartedEnded>,
    private val _game: Lazy<Game>,
    private val _dealer: Lazy<Player>,
    private val _hands: Lazy<List<PublicHand>>,
    private val _hand: Lazy<PrivateHand?>,
    private val _tricks: Lazy<List<Trick>>,
    private val _currentTrick: Lazy<Trick?>,
    private val _calls: Lazy<List<Call>>,
    private val _teamedResult: Lazy<TeamedResult?>,
    private val _turns: Lazy<List<Turn>>,
) {
    val cu: CreatedUpdated by _cu
    val se: StartedEnded by _se
    val game: Game by _game
    val dealer: Player by _dealer
    val hands: List<PublicHand> by _hands
    val hand: PrivateHand? by _hand
    val tricks: List<Trick> by _tricks
    val currentTrick: Trick? by _currentTrick
    val calls: List<Call> by _calls
    val result: TeamedResult? by _teamedResult
    val turns: List<Turn> by _turns

    constructor(entity: RoundEntity, currentUser: UserEntity) : this(
        id = entity.id,
        number = entity.number,
        state = entity.state,
        contract = entity.contract.roundContractPublic,
        _cu = lazy { CreatedUpdated(entity) },
        _se = lazy { StartedEnded(entity) },
        _game = lazy { Game(entity.game, currentUser) },
        _dealer = lazy { Player(entity.dealer, currentUser) },
        _hands = lazy { entity.hands.map { PublicHand(it, currentUser) } },
        _hand = lazy {
            currentUser.let {
                entity.hands.singleOrNull { h -> h.player.user.id == it.id }
            }?.let { PrivateHand(it, currentUser) }
        },
        _tricks = lazy { entity.tricks.map { Trick(it, currentUser) } },
        _currentTrick = lazy { entity.tricks.maxByOrNull { it.number }?.let { Trick(it, currentUser) } },
        _calls = lazy { entity.hands.flatMap { it.calls.map { c -> Call(c, currentUser) } } },
        _teamedResult = lazy {
            Teamed.from(entity.results) { it.team.internal }?.combine { re, ko ->
                TeamedResult(
                    re = RoundResult(re, currentUser),
                    ko = RoundResult(ko, currentUser)
                )
            }
        },
        _turns = lazy { entity.turns.map { Turn(it, currentUser) } }
    )
}
