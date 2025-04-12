package game.doppelkopf.core.model.hand

import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.persistence.model.hand.HandEntity

class HandModel(
    entity: HandEntity
) : HandModelAbstract(entity) {
    fun determineTeamByCards() {
        if (cardsRemaining.any { c -> c.isQueenOfClubs }) {
            internalTeam = Team.RE
            playerTeam = Team.RE
        } else {
            internalTeam = Team.KO
            playerTeam = Team.KO
        }
    }
}