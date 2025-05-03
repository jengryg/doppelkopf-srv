package game.doppelkopf.core.model.hand

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.persistence.model.hand.HandEntity

abstract class HandModelAbstract(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : IHandModel, IHandProperties by entity, ModelAbstract<HandEntity>(entity, factoryProvider) {
    override val round: IRoundModel
        get() = factoryProvider.round.create(entity.round)

    override val player: IPlayerModel
        get() = factoryProvider.player.create(entity.player)

    override val cards: List<Card>
        get() = round.deck.getCards(entity.cardsRemaining).getOrElse {
            throw GameFailedException("Can not decode the cards of hand $this.", entity.id, it)
        }

    override val size: Int
        get() = entity.cardsRemaining.size

    override fun assignPrivateTeam(team: Team) {
        internalTeam = team
        playerTeam = team
    }

    override fun assignPublicTeam(team: Team) {
        internalTeam = team
        playerTeam = team
        publicTeam = team
    }
}