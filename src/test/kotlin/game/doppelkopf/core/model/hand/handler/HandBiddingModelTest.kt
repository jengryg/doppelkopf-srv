package game.doppelkopf.core.model.hand.handler

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.model.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class HandBiddingModelTest : BaseUnitTest() {
    @Test
    fun `bid marriage when hand has marriage updates hand`() {
        val mfp = ModelFactoryProvider()

        val hand = HandBiddingModel(
            entity = createHandEntity(
                hasMarriage = true,
                declaration = Declaration.RESERVATION
            ),
            factoryProvider = mfp
        )

        val guard = hand.canBid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
        assertThat(guard.isSuccess).isTrue

        hand.bid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)

        assertThat(hand.bidding).isEqualTo(Bidding.MARRIAGE)
    }

    @Disabled("Until solo system is implemented, there is no entry in the enum except MARRIAGE available.")
    @ParameterizedTest
    @EnumSource(BiddingOption::class, names = ["MARRIAGE"], mode = EnumSource.Mode.EXCLUDE)
    fun `bid a solo updates hand`(biddingOption: BiddingOption) {
        val mfp = ModelFactoryProvider()

        val hand = HandBiddingModel(
            entity = createHandEntity(
                hasMarriage = true,
                declaration = Declaration.RESERVATION
            ),
            factoryProvider = mfp
        )

        val guard = hand.canBid(user = hand.player.user, biddingOption = biddingOption)
        assertThat(guard.isSuccess).isTrue

        hand.bid(user = hand.player.user, biddingOption = biddingOption)
        assertThat(hand.bidding).isEqualTo(biddingOption.internal)
    }

    @ParameterizedTest
    @EnumSource(value = BiddingOption::class)
    fun `guard yields exception when user is not owner of hand`(biddingOption: BiddingOption) {
        val mfp = ModelFactoryProvider()

        val hand = HandBiddingModel(
            entity = createHandEntity(
                hasMarriage = true,
                declaration = Declaration.RESERVATION
            ),
            factoryProvider = mfp
        )

        val user = mfp.user.create(entity = createUserEntity())

        val guard = hand.canBid(user = user, biddingOption = biddingOption)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform the action 'Bidding:Create': You can only bid on your own hand.")

        assertThatThrownBy {
            hand.bid(user = user, biddingOption = biddingOption)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform the action 'Bidding:Create': You can only bid on your own hand.")
    }

    @ParameterizedTest
    @EnumSource(value = Bidding::class, names = ["NOTHING"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when hand has already made a bid`(bidding: Bidding) {
        val mfp = ModelFactoryProvider()

        val hand = HandBiddingModel(
            entity = createHandEntity(
                hasMarriage = true,
                declaration = Declaration.RESERVATION,
                bidding = bidding
            ),
            factoryProvider = mfp
        )

        val guard = hand.canBid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bidding:Create' can not be performed: This hand has already made a bid.")

        assertThatThrownBy {
            hand.bid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bidding:Create' can not be performed: This hand has already made a bid.")
    }

    @ParameterizedTest
    @EnumSource(Declaration::class, names = ["RESERVATION"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when hand has not declared RESERVATION before`(declaration: Declaration) {
        val mfp = ModelFactoryProvider()

        val hand = HandBiddingModel(
            entity = createHandEntity(
                hasMarriage = true,
                declaration = declaration
            ),
            factoryProvider = mfp
        )

        val guard = hand.canBid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bidding:Create' can not be performed: You did not declared a RESERVATION, thus you can not bid.")

        assertThatThrownBy {
            hand.bid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bidding:Create' can not be performed: You did not declared a RESERVATION, thus you can not bid.")
    }

    @Test
    fun `guard yields exception when hand does not has marriage and declares MARRIAGE`() {
        val mfp = ModelFactoryProvider()

        val hand = HandBiddingModel(
            entity = createHandEntity(
                hasMarriage = false,
                declaration = Declaration.RESERVATION
            ),
            factoryProvider = mfp
        )

        val guard = hand.canBid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bidding:Create' can not be performed: You can only bid WEDDING when you have a marriage on hand.")

        assertThatThrownBy {
            hand.bid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bidding:Create' can not be performed: You can only bid WEDDING when you have a marriage on hand.")
    }
}