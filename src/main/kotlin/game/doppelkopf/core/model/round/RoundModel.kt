package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.persistence.model.round.RoundEntity

class RoundModel(
    entity: RoundEntity
) : RoundModelAbstract(entity) {
    /**
     * Configures this round according to the [RoundContract.NORMAL] with [DeckMode.DIAMONDS].
     */
    fun configureAsNormalRound() {
        hands.forEach {
            // Teams are just determined by QueenOfClubs, and each player is allowed to see their own team.
            it.determineTeamByCards()
        }
        deckMode = DeckMode.DIAMONDS
        state = RoundState.PLAYING_TRICKS
        contract = RoundContract.NORMAL
    }

    /**
     * Configures this round according to the [RoundContract.SILENT_MARRIAGE] with [DeckMode.DIAMONDS].
     */
    fun configureAsSilentMarriageRound() {
        hands.forEach {
            // Teams are just determined by QueenOfClubs, and each player is allowed to see their own team.
            // Silent Marriage means there are 3 KO and 1 RE players, as determined by the distribution of queen of clubs.
            it.determineTeamByCards()
        }
        deckMode = DeckMode.DIAMONDS
        state = RoundState.PLAYING_TRICKS
        contract = RoundContract.SILENT_MARRIAGE
    }

    /**
     * Configures this round according to the [RoundContract.WEDDING] with [DeckMode.DIAMONDS].
     */
    fun configureAsMarriageRound() {
        hands.forEach {
            when {
                it.hasMarriage -> {
                    // Team of player with marriage on hand is already public.
                    it.internalTeam = Team.RE
                    it.playerTeam = Team.RE
                    it.publicTeam = Team.RE
                }

                else -> {
                    // We consider the other players to be in no team until the marriage is resolved during the play.
                    it.internalTeam = Team.NA
                    it.playerTeam = Team.NA
                    it.playerTeam = Team.NA
                }
            }
        }
        deckMode = DeckMode.DIAMONDS
        state = RoundState.PLAYING_TRICKS
        contract = RoundContract.WEDDING
    }

    // TODO: SOLO SYSTEM IMPLEMENTATION
}