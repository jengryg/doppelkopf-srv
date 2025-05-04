package game.doppelkopf.core

import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.adapter.persistence.model.trick.TrickRepository
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnRepository
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.domain.round.handler.RoundMarriageResolverModel
import game.doppelkopf.domain.round.handler.RoundPlayCardModel
import game.doppelkopf.domain.trick.handler.TrickEvaluationModel
import org.springframework.stereotype.Service
import java.util.*

@Service
class TurnFacade(
    private val turnRepository: TurnRepository,
    private val trickRepository: TrickRepository,
    private val roundPersistence: RoundPersistence,
) {


    fun create(roundId: UUID, encodedCard: String, user: UserEntity): TurnEntity {
        val round = roundPersistence.load(roundId)

        val mfp = ModelFactoryProvider()

        val (trickEntity, turnEntity) = RoundPlayCardModel(round, mfp).playCard(
            encodedCard,
            mfp.user.create(user)
        ).let {
            Pair(
                it.first.entity,
                it.second.entity
            )
        }

        TrickEvaluationModel(entity = trickEntity, factoryProvider = mfp).also { trickEval ->
            trickEval.canEvaluateTrick().onSuccess {
                // Evaluate the trick if we can.
                trickEval.evaluateTrick()

                // A finished trick may trigger the marriage resolver:
                RoundMarriageResolverModel(entity = round, factoryProvider = mfp).also { marriageResolver ->
                    marriageResolver.canResolveMarriage().onSuccess {
                        // Resolve the marriage if possible.
                        marriageResolver.resolveMarriage()
                    }
                }
            }
        }

        trickRepository.save(trickEntity)

        return turnRepository.save(turnEntity)
    }
}