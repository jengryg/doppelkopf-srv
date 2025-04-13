package game.doppelkopf.core.model.hand

import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.persistence.model.hand.HandEntity

class HandModel(
    entity: HandEntity
) : HandModelAbstract(entity) {
    /**
     * Assigns [internalTeam] and [playerTeam] based on [cardsRemaining] containing the queen of clubs.
     * If the hand contains at least one queen of clubs, it is [Team.RE] otherwise it is [Team.KO].
     */
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