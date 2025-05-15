package game.doppelkopf.domain.hand.service

import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.common.errors.ofForbiddenAction
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.domain.call.model.ICallModel
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.hand.model.HandModelAbstract
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.user.model.IUserModel
import org.springframework.lang.CheckReturnValue

class HandCallModel(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : HandModelAbstract(entity, factoryProvider) {
    fun makeCall(user: IUserModel, callType: CallType): ICallModel {
        canMakeCall(user, callType).getOrThrow()

        return CallEntity(
            hand = entity,
            callType = callType,
            cardsPlayedBefore = playedCardCount
        ).let { factoryProvider.call.create(it) }.also {
            addCall(it)

            // making a call reveals the team of this hand
            revealTeam()
        }
    }

    @CheckReturnValue
    fun canMakeCall(user: IUserModel, callType: CallType): Result<Unit> {
        if (entity.player.user != user.entity) {
            return Result.ofForbiddenAction("You can only make a call on your own hand.")
        }

        if (round.state != RoundState.PLAYING_TRICKS) {
            return Result.ofInvalidAction("The round must be in ${RoundState.PLAYING_TRICKS} state to make a call.")
        }

        if (round.contract == RoundContract.MARRIAGE_UNRESOLVED) {
            return Result.ofInvalidAction("This round has an unresolved marriage. You must wait until the marriage is resolved to make calls.")
        }

        val callsOfTeam = when (internalTeam) {
            Team.RE -> round.getCalls().get(DefiniteTeam.RE)
            Team.KO -> round.getCalls().get(DefiniteTeam.KO)
            else -> return Result.ofInvalidAction("Your team is not yet determined. Thus, you can not make a call.")
        }

        if (callsOfTeam.singleOrNull { it.callType == callType } != null) {
            // Note: There seems to be no rule allowing to repeat a call of the own team (to reveal identity without impacting the scoring schema).
            // But there is also no rule that prohibits it. For this implementation, we prohibit it by our own judgement.
            return Result.ofInvalidAction("Your team already made this call. Repeating the call is not allowed.")
        }

        val requiredPreviousCall = callType.getPrevious()

        if (requiredPreviousCall != null) {
            // Player tries to make a call that requires a previous call to be present.
            // We do not allow to skip calls, players are forced to explicitly make each call.
            // This simplifies the implementation.
            callsOfTeam.singleOrNull { it.callType == callType.getPrevious() }
                ?: return Result.ofInvalidAction("Your team has not called ${requiredPreviousCall.publicIdentifiers.get(internalTeam)} before.")
        }

        val effectiveCardLimit = callType.cardLimit + round.determineCardCountOffset()

        return if (playedCardCount <= effectiveCardLimit) {
            Result.success(Unit)
        } else {
            Result.ofInvalidAction("This call can only be made with $effectiveCardLimit or less cards played, but you already played $playedCardCount cards.")
        }
    }
}