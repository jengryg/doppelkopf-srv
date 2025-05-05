package game.doppelkopf.domain.hand.ports.commands

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.hand.enums.BiddingOption

class HandCommandBid(
    val user: UserEntity,
    val hand: HandEntity,
    val biddingOption: BiddingOption
) : IHandCommand