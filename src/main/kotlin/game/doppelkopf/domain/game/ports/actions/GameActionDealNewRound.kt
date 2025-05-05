package game.doppelkopf.domain.game.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Deal a new round in the game given by [gameId] where [user] should be the dealer.
 */
class GameActionDealNewRound(
    override val user: UserDetails,
    override val gameId: UUID,
) : IGameAction