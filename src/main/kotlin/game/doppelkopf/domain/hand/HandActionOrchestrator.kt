package game.doppelkopf.domain.hand

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.hand.HandPersistence
import game.doppelkopf.domain.hand.ports.actions.HandActionBid
import game.doppelkopf.domain.hand.ports.actions.HandActionDeclare
import game.doppelkopf.domain.hand.ports.commands.HandCommandBid
import game.doppelkopf.domain.hand.ports.commands.HandCommandDeclare
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class HandActionOrchestrator(
    private val handPersistence: HandPersistence,
    private val handEngine: HandEngine
) {
    @Transactional
    fun execute(action: HandActionDeclare): HandEntity {
        val command = HandCommandDeclare(
            user = action.user.entity,
            hand = handPersistence.load(id = action.handId),
            declarationOption = action.declaration
        )

        return handEngine.execute(command)
    }

    @Transactional
    fun execute(action: HandActionBid): HandEntity {
        val command = HandCommandBid(
            user = action.user.entity,
            hand = handPersistence.load(id = action.handId),
            biddingOption = action.bid
        )

        return handEngine.execute(command)
    }
}