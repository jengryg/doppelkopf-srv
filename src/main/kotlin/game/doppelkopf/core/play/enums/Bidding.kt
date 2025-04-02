package game.doppelkopf.core.play.enums

import game.doppelkopf.core.cards.DeckMode

/**
 * [Bidding] is made by players that have declared RESERVATION.
 */
enum class Bidding(
    val roundContract: RoundContract,
    val deckMode: DeckMode,
) {
    NOTHING(roundContract = RoundContract.UNDECIDED, deckMode = DeckMode.DIAMONDS),

    WEDDING(roundContract = RoundContract.WEDDING, deckMode = DeckMode.DIAMONDS),

    SOLO_DIAMONDS(roundContract = RoundContract.SOLO, deckMode = DeckMode.DIAMONDS),
    SOLO_HEARTS(roundContract = RoundContract.SOLO, deckMode = DeckMode.HEARTS),
    SOLO_SPADES(roundContract = RoundContract.SOLO, deckMode = DeckMode.SPADES),
    SOLO_CLUBS(roundContract = RoundContract.SOLO, deckMode = DeckMode.CLUBS),

    SOLO_QUEENS(roundContract = RoundContract.SOLO, deckMode = DeckMode.QUEENS),
    SOLO_JACKS(roundContract = RoundContract.SOLO, deckMode = DeckMode.JACKS),

    SOLO_FREE(roundContract = RoundContract.SOLO, deckMode = DeckMode.FREE),
}

/**
 * [BiddingOption] is used to provide the choice to the player making it.
 * It prevents [Bidding.NOTHING] option, that is only for internal state management.
 */
enum class BiddingOption(
    val internal: Bidding,
) {
    WEDDING(Bidding.WEDDING),

    SOLO_DIAMONDS(Bidding.SOLO_DIAMONDS),
    SOLO_HEARTS(Bidding.SOLO_HEARTS),
    SOLO_SPADES(Bidding.SOLO_SPADES),
    SOLO_CLUBS(Bidding.SOLO_CLUBS),

    SOLO_QUEENS(Bidding.SOLO_QUEENS),
    SOLO_JACKS(Bidding.SOLO_JACKS),

    SOLO_FREE(Bidding.SOLO_FREE),
}