package game.doppelkopf.core.play.processor

import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.persistence.play.RoundEntity

object RoundConfigurator {
    /**
     * Configures the given [round] according to the [RoundContract.NORMAL] with [DeckMode.DIAMONDS].
     */
    fun configureNormalRound(round: RoundEntity) {
        round.hands.forEach {
            // Teams are just determined by QueenOfClubs, and each player is allowed to see their own team.
            it.playerTeam = it.internalTeam
        }
        round.deck = DeckMode.DIAMONDS
        round.state = RoundState.AUCTIONED
        round.contract = RoundContract.NORMAL
    }

    /**
     * Configures the given [round] according to the [RoundContract.SILENT_MARRIAGE] with [DeckMode.DIAMONDS].
     */
    fun configureSilentMarriageRound(round: RoundEntity) {
        round.hands.forEach {
            // Teams are just determined by QueenOfClubs, and each player is allowed to see their own team.
            // Silent Marriage means there are 3 KO and 1 RE players, as determined by the distribution of queen of clubs.
            it.playerTeam = it.internalTeam
        }
        round.deck = DeckMode.DIAMONDS
        round.state = RoundState.AUCTIONED
        round.contract = RoundContract.SILENT_MARRIAGE
    }

    /**
     * Configures the given [round] according to the [RoundContract.WEDDING] with [DeckMode.DIAMONDS].
     */
    fun configureMarriageRound(round: RoundEntity) {
        round.hands.forEach {
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
        round.deck = DeckMode.DIAMONDS
        round.state = RoundState.AUCTIONED
        round.contract = RoundContract.WEDDING
    }

//    /**
//     * Configures the given [round] according to the [RoundContract.SOLO] with [solo] as the hand that plays the solo
//     * and with the corresponding [DeckMode].
//     */
//    TODO: SOLO SYSTEM IMPLEMENTATION
//    fun configureSoloRound(round: RoundEntity) {
//
//        round.hands.forEach {
//            when {
//                it == solo -> {
//                    // The hand that is playing the solo must be marked as RE and the information is public.
//                    it.internalTeam = Team.RE
//                    it.playerTeam = Team.RE
//                    it.publicTeam = Team.RE
//                }
//
//                else -> {
//                    // The other hands must be marked as KO and the information is public.
//                    it.internalTeam = Team.KO
//                    it.playerTeam = Team.KO
//                    it.publicTeam = Team.KO
//                }
//            }
//        }
//        round.deck = solo.bidding.deckMode
//        round.state = RoundState.AUCTIONED
//        round.contract = RoundContract.SOLO
//    }
}