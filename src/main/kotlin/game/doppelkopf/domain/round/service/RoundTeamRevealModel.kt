package game.doppelkopf.domain.round.service

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.model.RoundModelAbstract

class RoundTeamRevealModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider) {
    fun revealTeamsIfPossible() {
        if (normalRoundTrigger() || silentMarriageRoundTrigger()) {
            // reveal the teams of all hands
            hands.values.forEach { h -> h.revealTeam() }
        }
    }

    private fun normalRoundTrigger(): Boolean {
        if (contract != RoundContract.NORMAL) {
            return false
        }

        if (allQueenOfClubsPlayed()) {
            return true
        }

        // if both RE players have revealed, it is public knowledge that this is a normal round and the teams are known
        // it is not enough to reveal two KO players, since we need to keep the information that this is NOT a
        // silent marriage round secret
        return 2 == hands.values.count { h -> h.publicTeam == Team.RE }
    }

    private fun silentMarriageRoundTrigger(): Boolean {
        if (contract != RoundContract.SILENT_MARRIAGE) {
            return false
        }

        if (allQueenOfClubsPlayed()) {
            return true
        }

        // if all three KO hands have revealed, it is public knowledge that this is a silent marriage round
        // it is not enough to reveal two KO players
        return 3 == hands.values.count { h -> h.publicTeam == Team.KO }
    }

    private fun allQueenOfClubsPlayed(): Boolean {
        // no queen of clubs remaining in the hand cards
        return 0 == hands.values.sumOf { h -> h.cards.count { it.isQueenOfClubs } }
    }
}