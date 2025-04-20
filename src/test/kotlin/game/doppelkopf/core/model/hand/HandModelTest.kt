package game.doppelkopf.core.model.hand

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.core.common.errors.ForbiddenActionException
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.user.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class HandModelTest : BaseUnitTest() {
    @Nested
    inner class Declare {
        @Test
        fun `declare healthy when hand does not have marriage updates hand`() {
            val hand = HandModel.create(entity = createHandEntity(hasMarriage = false))

            val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
            assertThat(guard.isSuccess).isTrue

            hand.declare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)

            assertThat(hand.declared).isEqualTo(Declaration.HEALTHY)
        }

        @Test
        fun `declare silent marriage when hand has marriage updates hand`() {
            val hand = HandModel.create(entity = createHandEntity(hasMarriage = true))

            val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)
            assertThat(guard.isSuccess).isTrue

            hand.declare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)

            assertThat(hand.declared).isEqualTo(Declaration.SILENT_MARRIAGE)
        }

        // @Disabled("Until solo system is implemented, declaring reservation without a marriage on hand will lead to a failed state.")
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `declare RESERVATION updates hand regardless of marriage status`(hasMarriage: Boolean) {
            val hand = HandModel.create(entity = createHandEntity(hasMarriage = hasMarriage))

            val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.RESERVATION)
            assertThat(guard.isSuccess).isTrue

            hand.declare(user = hand.player.user, declarationOption = DeclarationOption.RESERVATION)

            assertThat(hand.declared).isEqualTo(Declaration.RESERVATION)
        }

        @ParameterizedTest
        @EnumSource(DeclarationOption::class)
        fun `guard yields exception when user is not owner of hand`(declarationOption: DeclarationOption) {
            val hand = HandModel.create(entity = createHandEntity(hasMarriage = false))
            val user = UserModel.create(entity = UserEntity(username = "username", password = "password"))
            val guard = hand.canDeclare(user = user, declarationOption = declarationOption)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Bid:Create': You can only declare on your own hand.")

            assertThatThrownBy {
                hand.declare(user = user, declarationOption = declarationOption)
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Bid:Create': You can only declare on your own hand.")
        }

        @ParameterizedTest
        @EnumSource(Declaration::class, names = ["NOTHING"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when hand has already declared`(declaration: Declaration) {
            val hand = HandModel.create(
                entity = createHandEntity(
                    declaration = declaration,
                    hasMarriage = false
                )
            )

            val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: This hand has already made a declaration.")

            assertThatThrownBy {
                hand.declare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: This hand has already made a declaration.")
        }

        @Test
        fun `guard yields exception when hand has marriage and declares healthy`() {
            val hand = HandModel.create(entity = createHandEntity(hasMarriage = true))

            val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare HEALTHY when you have a marriage on hand.")

            assertThatThrownBy {
                hand.declare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare HEALTHY when you have a marriage on hand.")
        }

        @Test
        fun `guard yields exception when hand has no marriage and declares silent marriage`() {
            val hand = HandModel.create(entity = createHandEntity(hasMarriage = false))

            val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare SILENT_MARRIAGE when you are not having a marriage on hand.")

            assertThatThrownBy {
                hand.declare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare SILENT_MARRIAGE when you are not having a marriage on hand.")
        }
    }

    @Nested
    inner class Bid {
        @Test
        fun `bid marriage when hand has marriage updates hand`() {
            val hand = HandModel.create(
                entity = createHandEntity(
                    hasMarriage = true,
                    declaration = Declaration.RESERVATION
                )
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
            val hand = HandModel.create(
                entity = createHandEntity(
                    hasMarriage = true,
                    declaration = Declaration.RESERVATION
                )
            )

            val guard = hand.canBid(user = hand.player.user, biddingOption = biddingOption)
            assertThat(guard.isSuccess).isTrue

            hand.bid(user = hand.player.user, biddingOption = biddingOption)
            assertThat(hand.bidding).isEqualTo(biddingOption.internal)
        }

        @ParameterizedTest
        @EnumSource(value = BiddingOption::class)
        fun `guard yields exception when user is not owner of hand`(biddingOption: BiddingOption) {
            val hand = HandModel.create(
                entity = createHandEntity(
                    hasMarriage = true,
                    declaration = Declaration.RESERVATION
                )
            )
            val user = UserModel.create(entity = UserEntity(username = "username", password = "password"))

            val guard = hand.canBid(user = user, biddingOption = biddingOption)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Bid:Create': You can only bid on your own hand.")

            assertThatThrownBy {
                hand.bid(user = user, biddingOption = biddingOption)
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Bid:Create': You can only bid on your own hand.")
        }

        @ParameterizedTest
        @EnumSource(value = Bidding::class, names = ["NOTHING"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when hand has already made a bid`(bidding: Bidding) {
            val hand = HandModel.create(
                entity = createHandEntity(
                    hasMarriage = true,
                    declaration = Declaration.RESERVATION,
                    bidding = bidding
                ),
            )

            val guard = hand.canBid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: This hand has already made a bid.")

            assertThatThrownBy {
                hand.bid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: This hand has already made a bid.")
        }

        @ParameterizedTest
        @EnumSource(Declaration::class, names = ["RESERVATION"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when hand has not declared RESERVATION before`(declaration: Declaration) {
            val hand = HandModel.create(
                entity = createHandEntity(
                    hasMarriage = true,
                    declaration = declaration
                )
            )

            val guard = hand.canBid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You did not declared a RESERVATION, thus you can not bid.")

            assertThatThrownBy {
                hand.bid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You did not declared a RESERVATION, thus you can not bid.")
        }

        @Test
        fun `guard yields exception when hand does not has marriage and declares MARRIAGE`() {
            val hand = HandModel.create(
                entity = createHandEntity(
                    hasMarriage = false,
                    declaration = Declaration.RESERVATION
                )
            )

            val guard = hand.canBid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You can only bid WEDDING when you have a marriage on hand.")

            assertThatThrownBy {
                hand.bid(user = hand.player.user, biddingOption = BiddingOption.MARRIAGE)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bid:Create' can not be performed: You can only bid WEDDING when you have a marriage on hand.")
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `create uses one model per entity`() {
            val entity = createHandEntity()

            val model = HandModel.create(entity)
            val other = HandModel.create(entity)

            assertThat(model).isSameAs(other)
        }
    }
}