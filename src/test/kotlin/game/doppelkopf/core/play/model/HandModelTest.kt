package game.doppelkopf.core.play.model

import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.persistence.play.HandEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HandModelTest {
    @Nested
    inner class CreateDeclaration {
        @Test
        fun `declare on hand that has already declared throws exception`() {
            val hand = createHandEntity(marriage = false, declaration = Declaration.HEALTHY)

            assertThatThrownBy {
                HandModel(hand).declare(DeclarationOption.HEALTHY)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("")
        }

        @Test
        fun `declare healthy when hand has marriage throws exception`() {
            val hand = createHandEntity(marriage = true)

            assertThatThrownBy {
                HandModel(hand).declare(DeclarationOption.HEALTHY)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("")
        }

        @Test
        fun `declare healthy when hand hand does not have marriage updates HandEntity`() {
            val hand = createHandEntity(marriage = false)

            HandModel(hand).declare(DeclarationOption.HEALTHY)

            assertThat(hand.declared).isEqualTo(Declaration.HEALTHY)
        }

        @Test
        fun `declare silent marriage when hand does not have marriage throws exception`() {
            val hand = createHandEntity(marriage = false)

            assertThatThrownBy {
                HandModel(hand).declare(DeclarationOption.SILENT_MARRIAGE)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("")
        }

        @Test
        fun `declare silent marriage when hand has marriage updates HandEntity`() {
            val hand = createHandEntity(marriage = true)

            HandModel(hand).declare(DeclarationOption.SILENT_MARRIAGE)

            assertThat(hand.declared).isEqualTo(Declaration.SILENT_MARRIAGE)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `declare reservation updates HandEntity`(hasMarriage: Boolean) {
            val hand = createHandEntity(marriage = hasMarriage)

            HandModel(hand).declare(DeclarationOption.RESERVATION)

            assertThat(hand.declared).isEqualTo(Declaration.RESERVATION)
        }
    }

    @Nested
    inner class CreateBid {
        @Test
        fun `bid on hand that has already bid throws exception`() {
            val hand = createHandEntity(marriage = true, declaration = Declaration.RESERVATION, bid = Bidding.WEDDING)

            assertThatThrownBy {
                HandModel(hand).bid(BiddingOption.WEDDING)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("")
        }

        @ParameterizedTest
        @EnumSource(Declaration::class, names = ["RESERVATION"], mode = EnumSource.Mode.EXCLUDE)
        fun `bid on hand that did not declared reservation throws exception`(declaration: Declaration) {
            val hand = createHandEntity(marriage = true, declaration = declaration)

            assertThatThrownBy {
                HandModel(hand).bid(BiddingOption.WEDDING)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("")
        }

        @Test
        fun `bid a wedding when hand does not have marriage throws exception`() {
            val hand = createHandEntity(marriage = false, declaration = Declaration.RESERVATION)

            assertThatThrownBy {
                HandModel(hand).bid(BiddingOption.WEDDING)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("")
        }

        @Test
        fun `bid a wedding when hand has marriage updates HandEntity`() {
            val hand = createHandEntity(marriage = true, declaration = Declaration.RESERVATION)

            HandModel(hand).bid(BiddingOption.WEDDING)

            assertThat(hand.bidding).isEqualTo(Bidding.WEDDING)
        }

//        TODO: SOLO SYSTEM IMPLEMENTATION
//        @ParameterizedTest
//        @EnumSource(BiddingOption::class, names = ["WEDDING"], mode = EnumSource.Mode.EXCLUDE)
//        fun `bid a solo when hand has declared reservation updates HandEntity`(bidOption: BiddingOption) {
//            val hand = createHandEntity(marriage = false, declaration = Declaration.RESERVATION)
//
//            HandModel(hand).bid(bidOption)
//
//            assertThat(hand.bidding).isEqualTo(bidOption.internal)
//        }
    }

    private fun createHandEntity(
        marriage: Boolean,
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