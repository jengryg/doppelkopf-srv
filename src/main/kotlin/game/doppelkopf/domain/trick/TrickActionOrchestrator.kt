package game.doppelkopf.domain.trick

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.domain.trick.ports.actions.TrickActionEvaluate
import game.doppelkopf.domain.trick.ports.commands.TrickCommandEvaluate
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TrickActionOrchestrator(
    private val trickPersistence: TrickPersistence,
    private val trickEngine: TrickEngine
) {
    @Transactional
    fun execute(action: TrickActionEvaluate): TrickEntity {
        val command = TrickCommandEvaluate(
            trick = trickPersistence.load(action.trickId)
        )

        return trickEngine.execute(command)
    }
}