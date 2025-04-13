package game.doppelkopf.core.handler.hand

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.persistence.model.hand.HandEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HandBiddingHandlerTest {
    @Nested
    inner class DoHandle {
        @Test
        fun `bid marriage when hand has marriage updates hand`() {
            val hand = HandModel(createHandEntity(marriage = true, declaration = Declaration.RESERVATION))

            val handler = HandBiddingHandler(hand)

            val result = handler.doHandle(BiddingOption.WEDDING)

            assertThat(result).isEqualTo(hand.entity)
            assertThat(result.bidding).isEqualTo(Bidding.WEDDING)
        }

        @Disabled("Until solo system is implemented, there is no entry in the enum except WEDDING.")
        @ParameterizedTest
        @EnumSource(BiddingOption::class, names = ["WEDDING"], mode = EnumSource.Mode.EXCLUDE)
        fun `bid a solo updates hand`(biddingOption: BiddingOption) {
            val hand = HandModel(createHandEntity(marriage = false, declaration = Declaration.RESERVATION))

            val handler = HandBiddingHandler(hand)

            val result = handler.doHandle(biddingOption)

            assertThat(result).isEqualTo(hand.entity)
            assertThat(result.bidding).isEqualTo(biddingOption.internal)
        }
    }

    @Nested
    inner class GuardBlockingCases {
        @ParameterizedTest
        @EnumSource(value = Bidding::class, names = ["NOTHING"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when hand has already made a bid`(declaration: Bidding) {
            val hand = createHandEntity(marriage = true, declaration = Declaration.RESERVATION, bid = Bidding.WEDDING)

            val handler = HandBiddingHandler(
                hand = HandModel(hand)
            )

            val guard = handler.canHandle(BiddingOption.WEDDING)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: This hand has already made a bid.")

            assertThatThrownBy {
                handler.doHandle(BiddingOption.WEDDING)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: This hand has already made a bid.")
        }

        @ParameterizedTest
        @EnumSource(Declaration::class, names = ["RESERVATION"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when hand has not declared RESERVATION before`(declaration: Declaration) {
            val hand = createHandEntity(marriage = true, declaration = declaration)

            val handler = HandBiddingHandler(
                hand = HandModel(hand)
            )

            val guard = handler.canHandle(BiddingOption.WEDDING)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You did not declared a RESERVATION, thus you can not bid.")

            assertThatThrownBy {
                handler.doHandle(BiddingOption.WEDDING)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You did not declared a RESERVATION, thus you can not bid.")
        }

        @Test
        fun `guard yields exception when hand does not has marriage and declares MARRIAGE`() {
            val hand = createHandEntity(marriage = false, declaration = Declaration.RESERVATION)

            val handler = HandBiddingHandler(
                hand = HandModel(hand)
            )

            val guard = handler.canHandle(BiddingOption.WEDDING)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You can only bid WEDDING when you have a marriage on hand.")

            assertThatThrownBy {
                handler.doHandle(BiddingOption.WEDDING)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You can only bid WEDDING when you have a marriage on hand.")
        }
    }

    private fun createHandEntity(
        marriage: Boolean = false,
        declaration: Declaration = Declaration.NOTHING,
        bid: Bidding = Bidding.NOTHING
    ): HandEntity {
        return HandEntity(
            round = mockk(),
            player = mockk(),
            cardsRemaining = mutableListOf(),
            hasMarriage = marriage
        ).apply {
            declared = declaration
            bidding = bid
        }
    }
}