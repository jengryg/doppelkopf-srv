package game.doppelkopf.core.play.model

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.play.HandEntity
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.utils.Quadruple

class RoundModel(
    val round: RoundEntity,
    val deck: Deck,
) : Logging {
    private val log = logger()

    /**
     * Initialize the [HandEntity] with cards from the [deck] for each of the players given by [activePlayers] and
     * assigns the [HandEntity] to this [round] and the corresponding [PlayerEntity].
     *
     * @return the [Quadruple] of the initialized [HandEntity]
     */
    fun createPlayerHands(activePlayers: Quadruple<PlayerEntity>): Quadruple<HandEntity> {
        // No rankings or deck rules are required, just the cards in the deck shuffled and distributed into 4 lists.
        val handCards = deck.dealHandCards()

        return Quadruple(
            first = createHandEntity(activePlayers.first, handCards.first),
            second = createHandEntity(activePlayers.second, handCards.second),
            third = createHandEntity(activePlayers.third, handCards.third),
            fourth = createHandEntity(activePlayers.fourth, handCards.fourth),
        )
    }

    /**
     * Initializes a [HandEntity] for the given [player] with the given [cards] and assigns it to this [round].
     */
    private fun createHandEntity(player: PlayerEntity, cards: List<Card>): HandEntity {
        val encodedCards = cards.map { it.encoded }

        return HandEntity(
            round = round,
            player = player,
            cardsRemaining = encodedCards.toMutableList(),
            hasMarriage = cards.count { it.isQueenOfClubs } == 2
        ).also {
            // When cards are dealt, each player knows its own (potential) team before auction phase.
            if (cards.any { c -> c.isQueenOfClubs }) {
                it.internalTeam = Team.RE
                it.playerTeam = Team.RE
            } else {
                it.internalTeam = Team.KO
                it.playerTeam = Team.KO
            }

            round.hands.add(it)
        }
    }
}