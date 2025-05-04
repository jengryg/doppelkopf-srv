package game.doppelkopf.domain.hand.model

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.common.errors.GameFailedException
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.round.model.IRoundModel

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