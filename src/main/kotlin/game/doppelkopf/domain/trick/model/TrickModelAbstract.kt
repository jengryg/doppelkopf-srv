package game.doppelkopf.domain.trick.model

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.common.enums.TrickState
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.round.model.IRoundModel

abstract class TrickModelAbstract(
    entity: TrickEntity,
    factoryProvider: ModelFactoryProvider
) : ITrickModel, ITrickProperties by entity, ModelAbstract<TrickEntity>(entity, factoryProvider) {
    override val round: IRoundModel
        get() = factoryProvider.round.create(entity.round)

    override val winner: IHandModel?
        get() = entity.winner?.let { factoryProvider.hand.create(it) }

    override fun setWinner(winner: IHandModel) {
        if (entity.winner != null) {
            throw GameFailedException("Winner of trick $this is already set.", entity.id)
        }
        entity.winner = winner.entity
    }

    override val size: Int
        get() = entity.cards.size

    override val cards: List<Card>
        get() = round.deck.getCards(entity.cards).getOrElse {
            throw GameFailedException("Can not decode the cards of trick $this.", entity.id, it)
        }

    override fun getExpectedHandIndex(): Int {
        // Since each card played advanced the expected hand index by 1, we can calculate the index of the hand that
        // is expected to play the next card in the trick using:
        return (openIndex + size) % 4
    }

    override fun determineLeadingCardIndex(): Int {
        if (size <= 1) {
            return 0
        }

        // Obtain the minimal ranking value of all cards, the lower the ranking value the better the card.
        val minRanking = cards.minOf { it.ranking }
        // Find the first card that has the minimal ranking.
        // Return the index of the first minimal ranking card, to resolve ambiguity if minimal ranking is not unique.
        return cards.indexOfFirst { it.ranking == minRanking }
    }

    override fun determineScoreFromCards(): Int {
        return cards.sumOf { it.kind.points }
    }

    override fun determineStateFromCards(): TrickState {
        return when (size) {
            1 -> TrickState.FIRST_CARD_PLAYED
            2 -> TrickState.SECOND_CARD_PLAYED
            3 -> TrickState.THIRD_CARD_PLAYED
            4 -> TrickState.FOURTH_CARD_PLAYED
            else -> throw GameFailedException("Can not determine state of trick with $size cards.", entity.id)
        }
    }

    override fun updateCachedValues() {
        if (size <= 0) {
            // if this trick does not contain any cards, we do not update
            return
        }

        // cache the current score of the trick
        score = determineScoreFromCards()
        // cache the current leading card index of the trick
        leadingCardIndex = determineLeadingCardIndex()
        // advance the state of the trick
        state = determineStateFromCards()
    }
}