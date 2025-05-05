package game.doppelkopf.domain.round.ports.commands

import game.doppelkopf.adapter.persistence.model.round.RoundEntity

class RoundCommandEvaluateBids(
    val round: RoundEntity
) : IRoundCommand