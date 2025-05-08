package game.doppelkopf.domain.round

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.round.ports.actions.RoundActionEvaluate
import game.doppelkopf.domain.round.ports.actions.RoundActionEvaluateBids
import game.doppelkopf.domain.round.ports.actions.RoundActionEvaluateDeclarations
import game.doppelkopf.domain.round.ports.actions.RoundActionPlayCard
import game.doppelkopf.domain.round.ports.actions.RoundActionResolveMarriage
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluate
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluateBids
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluateDeclarations
import game.doppelkopf.domain.round.ports.commands.RoundCommandPlayCard
import game.doppelkopf.domain.round.ports.commands.RoundCommandResolveMarriage
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RoundActionOrchestrator(
    private val roundPersistence: RoundPersistence,
    private val roundEngine: RoundEngine
) {
    @Transactional
    fun execute(action: RoundActionPlayCard): TurnEntity {
        val command = roundPersistence.load(action.roundId).let {
            RoundCommandPlayCard(
                user = action.user.entity,
                round = it,
                card = Deck.create(it.deckMode).getCard(action.encodedCard).getOrThrow()
            )
        }

        return roundEngine.execute(command)
    }

    @Transactional
    fun execute(action: RoundActionResolveMarriage): RoundEntity {
        val command = RoundCommandResolveMarriage(
            round = roundPersistence.load(action.roundId)
        )

        return roundEngine.execute(command)
    }

    @Transactional
    fun execute(action: RoundActionEvaluateDeclarations): RoundEntity {
        val command = RoundCommandEvaluateDeclarations(
            round = roundPersistence.load(action.roundId)
        )

        return roundEngine.execute(command)
    }

    @Transactional
    fun execute(action: RoundActionEvaluateBids) : RoundEntity {
        val command = RoundCommandEvaluateBids(
            round = roundPersistence.load(action.roundId)
        )

        return roundEngine.execute(command)
    }

    @Transactional
    fun execute(action: RoundActionEvaluate) : RoundEntity {
        val command = RoundCommandEvaluate(
            round = roundPersistence.load(action.roundId)
        )

        return roundEngine.execute(command)
    }
}