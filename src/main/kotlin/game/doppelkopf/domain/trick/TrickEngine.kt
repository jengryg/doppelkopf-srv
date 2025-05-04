package game.doppelkopf.domain.trick

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.trick.ports.commands.ITrickCommand
import game.doppelkopf.domain.trick.ports.commands.TrickCommandEvaluate
import game.doppelkopf.domain.trick.service.TrickEvaluationModel
import game.doppelkopf.domain.user.model.IUserModel
import org.springframework.stereotype.Service

@Service
class TrickEngine(private val trickPersistence: TrickPersistence) {
    fun execute(command: TrickCommandEvaluate): TrickEntity {
        val resources = prepareResources(command)

        TrickEvaluationModel(
            entity = resources.trick,
            factoryProvider = resources.mfp
        ).evaluateTrick()

        return resources.trick
    }

    private fun prepareResources(command: ITrickCommand): TrickCommandResources {
        val mfp = ModelFactoryProvider()

        val trick = trickPersistence.load(command.trickId)
        val user = mfp.user.create(command.user.entity)

        return TrickCommandResources(
            user = user,
            trick = trick,
            mfp = mfp
        )
    }

    inner class TrickCommandResources(
        val user: IUserModel,
        val trick: TrickEntity,
        val mfp: ModelFactoryProvider
    )
}