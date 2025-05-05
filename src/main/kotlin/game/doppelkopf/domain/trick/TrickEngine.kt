package game.doppelkopf.domain.trick

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.round.RoundEngine
import game.doppelkopf.domain.round.ports.commands.RoundCommandResolveMarriage
import game.doppelkopf.domain.trick.ports.commands.TrickCommandEvaluate
import game.doppelkopf.domain.trick.service.TrickEvaluationModel
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class TrickEngine(
    @Lazy
    private val roundEngine: RoundEngine
) {
    fun execute(command: TrickCommandEvaluate): TrickEntity {
        val mfp = ModelFactoryProvider()

        TrickEvaluationModel(
            entity = command.trick,
            factoryProvider = mfp
        ).evaluateTrick()

        // TODO: the engine execute methods are throwing the occurring exceptions, thus we need to silence them for now
        //  later this should be changed to a more resilient implementation where the engines are not throwing
        runCatching {
            roundEngine.execute(
                command = RoundCommandResolveMarriage(
                    round = command.trick.round
                )
            )
            // just ignore the exceptions here
        }

        return command.trick
    }
}