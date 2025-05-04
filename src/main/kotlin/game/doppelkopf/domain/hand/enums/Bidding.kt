package game.doppelkopf.domain.hand.enums

import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.deck.enums.DeckMode

/**
 * [Bidding] is made by players that have declared RESERVATION.
 */
enum class Bidding(
    val roundContract: RoundContract,
    val deckMode: DeckMode,
    val isSolo: Boolean,
) {
    NOTHING(roundContract = RoundContract.UNDECIDED, deckMode = DeckMode.DIAMONDS, isSolo = false),

    MARRIAGE(roundContract = RoundContract.MARRIAGE_UNRESOLVED, deckMode = DeckMode.DIAMONDS, isSolo = false),

    SOLO_DIAMONDS(roundContract = RoundContract.SOLO, deckMode = DeckMode.DIAMONDS, isSolo = true),
    SOLO_HEARTS(roundContract = RoundContract.SOLO, deckMode = DeckMode.HEARTS, isSolo = true),
    SOLO_SPADES(roundContract = RoundContract.SOLO, deckMode = DeckMode.SPADES, isSolo = true),
    SOLO_CLUBS(roundContract = RoundContract.SOLO, deckMode = DeckMode.CLUBS, isSolo = true),

    SOLO_QUEENS(roundContract = RoundContract.SOLO, deckMode = DeckMode.QUEENS, isSolo = true),
    SOLO_JACKS(roundContract = RoundContract.SOLO, deckMode = DeckMode.JACKS, isSolo = true),

    SOLO_FREE(roundContract = RoundContract.SOLO, deckMode = DeckMode.FREE, isSolo = true),
}

/**
 * [BiddingOption] is used to provide the choice to the player making it.
 * It prevents [Bidding.NOTHING] option, that is only for internal state management.
 */
enum class BiddingOption(
    val internal: Bidding,
) {
    MARRIAGE(Bidding.MARRIAGE),

//    TODO: SOLO SYSTEM IMPLEMENTATION
//    SOLO_DIAMONDS(Bidding.SOLO_DIAMONDS),
//    SOLO_HEARTS(Bidding.SOLO_HEARTS),
//    SOLO_SPADES(Bidding.SOLO_SPADES),
//    SOLO_CLUBS(Bidding.SOLO_CLUBS),
//
//    SOLO_QUEENS(Bidding.SOLO_QUEENS),
//    SOLO_JACKS(Bidding.SOLO_JACKS),
//
//    SOLO_FREE(Bidding.SOLO_FREE),
}