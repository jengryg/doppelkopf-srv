package game.doppelkopf.domain.hand.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.enums.RoundState
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class HandCallModelTest : BaseUnitTest() {
    @Test
    fun `call 120 in time creates entity and updates hand`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.NORMAL
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round).apply {
                internalTeam = Team.RE
                publicTeam = Team.NA
                cardsPlayed.add("QC0")
            },
            factoryProvider = mfp
        )

        val call = hand.makeCall(hand.player.user, CallType.UNDER_120)

        assertThat(call.hand).isEqualTo(hand)
        assertThat(call.callType).isEqualTo(CallType.UNDER_120)
        assertThat(call.cardsPlayedBefore).isEqualTo(1)

        assertThat(hand.publicTeam).isEqualTo(Team.RE)
        assertThat(hand.calls).hasSize(1)
    }

    @Test
    fun `call 90 in time with 120 already called creates entity and updates hand`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.NORMAL
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round).apply {
                internalTeam = Team.RE
                repeat(2) { cardsPlayed.add("QC0") }
                calls.add(CallEntity(hand = this, CallType.UNDER_120, cardsPlayedBefore = 1))
                round.hands.add(this)
            },
            factoryProvider = mfp
        )

        val call = hand.makeCall(hand.player.user, CallType.UNDER_90)

        assertThat(call.hand).isEqualTo(hand)
        assertThat(call.callType).isEqualTo(CallType.UNDER_90)
        assertThat(call.cardsPlayedBefore).isEqualTo(2)

        assertThat(hand.publicTeam).isEqualTo(Team.RE)
        assertThat(hand.calls).hasSize(2)
    }

    @ParameterizedTest
    @EnumSource(RoundState::class, names = ["PLAYING_TRICKS"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when round is not in correct state`(roundState: RoundState) {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = roundState
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round),
            factoryProvider = mfp
        )

        val guard = hand.canMakeCall(hand.player.user, CallType.UNDER_120)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The round must be in PLAYING_TRICKS state to make a call.")

        assertThatThrownBy {
            hand.makeCall(hand.player.user, CallType.UNDER_120)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The round must be in PLAYING_TRICKS state to make a call.")
    }

    @Test
    fun `guard yields exception when round has unresolved marriage`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.MARRIAGE_UNRESOLVED
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round),
            factoryProvider = mfp
        )

        val guard = hand.canMakeCall(hand.player.user, CallType.UNDER_120)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: This round has an unresolved marriage. You must wait until the marriage is resolved to make calls.")

        assertThatThrownBy {
            hand.makeCall(hand.player.user, CallType.UNDER_120)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: This round has an unresolved marriage. You must wait until the marriage is resolved to make calls.")
    }

    @Test
    fun `guard yields exception when user is not owner of hand`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.NORMAL
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round),
            factoryProvider = mfp
        )

        val user = mfp.user.create(entity = createUserEntity())

        val guard = hand.canMakeCall(user, CallType.UNDER_120)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: You can only make a call on your own hand.")

        assertThatThrownBy {
            hand.makeCall(user, CallType.UNDER_120)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: You can only make a call on your own hand.")
    }

    @Test
    fun `guard yields exception when team of hand is not definite`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.NORMAL
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round),
            factoryProvider = mfp
        )

        val guard = hand.canMakeCall(hand.player.user, CallType.UNDER_120)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Your team is not yet determined. Thus, you can not make a call.")

        assertThatThrownBy {
            hand.makeCall(hand.player.user, CallType.UNDER_120)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Your team is not yet determined. Thus, you can not make a call.")
    }

    @Test
    fun `guard yields exception when call is repeated`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.NORMAL
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round).apply {
                internalTeam = Team.RE
                calls.add(CallEntity(hand = this, callType = CallType.UNDER_120, cardsPlayedBefore = 1))
                round.hands.add(this)
            },
            factoryProvider = mfp
        )

        val guard = hand.canMakeCall(hand.player.user, CallType.UNDER_120)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Your team already made this call. Repeating the call is not allowed.")

        assertThatThrownBy {
            hand.makeCall(hand.player.user, CallType.UNDER_120)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Your team already made this call. Repeating the call is not allowed.")
    }

    @Test
    fun `guard yields exception when call types are skipped`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.NORMAL
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round).apply {
                internalTeam = Team.RE
                round.hands.add(this)
            },
            factoryProvider = mfp
        )

        val guard = hand.canMakeCall(hand.player.user, CallType.UNDER_90)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Your team has not called RE before.")

        assertThatThrownBy {
            hand.makeCall(hand.player.user, CallType.UNDER_90)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Your team has not called RE before.")
    }

    @Test
    fun `guard yields exception when call is made to late`() {
        val mfp = ModelFactoryProvider()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
            contract = RoundContract.NORMAL
        }

        val hand = HandCallModel(
            entity = createHandEntity(round = round).apply {
                internalTeam = Team.RE
                repeat(2) { cardsPlayed.add("QC0") }
            },
            factoryProvider = mfp
        )

        val guard = hand.canMakeCall(hand.player.user, CallType.UNDER_120)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: This call can only be made with 1 or less cards played, but you already played 2 cards.")

        assertThatThrownBy {
            hand.makeCall(hand.player.user, CallType.UNDER_120)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: This call can only be made with 1 or less cards played, but you already played 2 cards.")
    }
}