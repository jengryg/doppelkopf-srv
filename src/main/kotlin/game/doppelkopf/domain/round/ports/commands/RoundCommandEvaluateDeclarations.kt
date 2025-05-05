package game.doppelkopf.domain.round.ports.commands

import game.doppelkopf.adapter.persistence.model.round.RoundEntity

class RoundCommandEvaluateDeclarations(
    val round: RoundEntity
) : IRoundCommand