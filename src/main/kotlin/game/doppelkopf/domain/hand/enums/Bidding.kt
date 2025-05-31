@file:Suppress("unused")

package game.doppelkopf.domain.hand.enums

import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.round.enums.RoundContract

/**
 * [Bidding] is made by players that have declared RESERVATION.
 */
enum class Bidding(
    val roundContract: RoundContract,
    val deckMode: DeckMode,
    val isSolo: Boolean,
    val biddingPublic: BiddingPublic,
) {
    NOTHING(
        roundContract = RoundContract.UNDECIDED,
        deckMode = DeckMode.DIAMONDS,
        isSolo = false,
        biddingPublic = BiddingPublic.NOTHING
    ),

    MARRIAGE(
        roundContract = RoundContract.MARRIAGE_UNRESOLVED,
        deckMode = DeckMode.DIAMONDS,
        isSolo = false,
        biddingPublic = BiddingPublic.MARRIAGE
    ),

    SOLO_DIAMONDS(
        roundContract = RoundContract.SOLO,
        deckMode = DeckMode.DIAMONDS,
        isSolo = true,
        biddingPublic = BiddingPublic.SOLO_DIAMONDS
    ),
    SOLO_HEARTS(
        roundContract = RoundContract.SOLO,
        deckMode = DeckMode.HEARTS,
        isSolo = true,
        biddingPublic = BiddingPublic.SOLO_HEARTS
    ),
    SOLO_SPADES(
        roundContract = RoundContract.SOLO,
        deckMode = DeckMode.SPADES,
        isSolo = true,
        biddingPublic = BiddingPublic.SOLO_SPADES
    ),
    SOLO_CLUBS(
        roundContract = RoundContract.SOLO,
        deckMode = DeckMode.CLUBS,
        isSolo = true,
        biddingPublic = BiddingPublic.SOLO_CLUBS
    ),

    SOLO_QUEENS(
        roundContract = RoundContract.SOLO,
        deckMode = DeckMode.QUEENS,
        isSolo = true,
        biddingPublic = BiddingPublic.SOLO_QUEENS
    ),
    SOLO_JACKS(
        roundContract = RoundContract.SOLO,
        deckMode = DeckMode.JACKS,
        isSolo = true,
        biddingPublic = BiddingPublic.SOLO_JACKS
    ),

    SOLO_FREE(
        roundContract = RoundContract.SOLO,
        deckMode = DeckMode.FREE,
        isSolo = true,
        biddingPublic = BiddingPublic.SOLO_FREE
    ),
}

/**
 * [BiddingPublic] is used to communicate the choice of the players to each other.
 * It does not directly hide something, but for consistency to the declarations we use it.
 */
enum class BiddingPublic {
    NOTHING,

    MARRIAGE,

    SOLO_DIAMONDS,
    SOLO_HEARTS,
    SOLO_SPADES,
    SOLO_CLUBS,

    SOLO_QUEENS,
    SOLO_JACKS,

    SOLO_FREE,
}

/**
 * [BiddingOption] is used to provide the choice to the player making it.
 * It prevents [Bidding.NOTHING] option, that is only for internal state management.
 */
enum class BiddingOption(
    val internal: Bidding,
) {
    MARRIAGE(Bidding.MARRIAGE),

    SOLO_DIAMONDS(Bidding.SOLO_DIAMONDS),
    SOLO_HEARTS(Bidding.SOLO_HEARTS),
    SOLO_SPADES(Bidding.SOLO_SPADES),
    SOLO_CLUBS(Bidding.SOLO_CLUBS),

    SOLO_QUEENS(Bidding.SOLO_QUEENS),
    SOLO_JACKS(Bidding.SOLO_JACKS),

    SOLO_FREE(Bidding.SOLO_FREE),
}