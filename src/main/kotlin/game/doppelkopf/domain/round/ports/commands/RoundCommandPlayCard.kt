package game.doppelkopf.domain.round.ports.commands

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.deck.model.Card

class RoundCommandPlayCard(
    val user: UserEntity,
    val round: RoundEntity,
    val card: Card
) : IRoundCommand