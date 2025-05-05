package game.doppelkopf.domain.trick.ports.commands

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity

class TrickCommandEvaluate(
    val trick: TrickEntity
) : ITrickCommand