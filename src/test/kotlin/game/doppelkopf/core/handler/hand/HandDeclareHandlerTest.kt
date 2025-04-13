package game.doppelkopf.core.handler.hand

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.persistence.model.hand.HandEntity
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
class HandDeclareHandlerTest {
    @Nested
    inner class DoHandle {
        @Test
        fun `declare healthy when hand does not has marriage updates hand`() {
            val hand = HandModel(createHandEntity(marriage = false))

            val handler = HandDeclareHandler(hand = hand)

            val result = handler.doHandle(DeclarationOption.HEALTHY)

            assertThat(result).isEqualTo(hand.entity)
            assertThat(result.declared).isEqualTo(Declaration.HEALTHY)
        }

        @Test
        fun `declare silent marriage when hand has marriage updates hand`() {
            val hand = HandModel(createHandEntity(marriage = true))

            val handler = HandDeclareHandler(hand = hand)

            val result = handler.doHandle(DeclarationOption.SILENT_MARRIAGE)

            assertThat(result).isEqualTo(hand.entity)
            assertThat(result.declared).isEqualTo(Declaration.SILENT_MARRIAGE)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `declare reservation updates hand`(hasMarriage: Boolean) {
            val hand = HandModel(createHandEntity(marriage = hasMarriage))

            val handler = HandDeclareHandler(hand = hand)

            val result = handler.doHandle(DeclarationOption.RESERVATION)

            assertThat(result).isEqualTo(hand.entity)
            assertThat(result.declared).isEqualTo(Declaration.RESERVATION)
        }
    }

    @Nested
    inner class GuardBlockingCases {
        @ParameterizedTest
        @EnumSource(Declaration::class, names = ["NOTHING"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when hand has already declared`(declaration: Declaration) {
            val hand = createHandEntity(declaration = declaration)

            val handler = HandDeclareHandler(
                hand = HandModel(hand),
            )

            val guard = handler.canHandle(DeclarationOption.HEALTHY)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: This hand has already made a declaration.")

            assertThatThrownBy {
                handler.doHandle(DeclarationOption.HEALTHY)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: This hand has already made a declaration.")
        }

        @Test
        fun `guard yields exception when hand has marriage and declares healthy`() {
            val hand = createHandEntity(marriage = true)

            val handler = HandDeclareHandler(
                hand = HandModel(hand),
            )

            val guard = handler.canHandle(DeclarationOption.HEALTHY)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare HEALTHY when you have a marriage on hand.")

            assertThatThrownBy {
                handler.doHandle(DeclarationOption.HEALTHY)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare HEALTHY when you have a marriage on hand.")
        }

        @Test
        fun `guard yields exception when hand has no marriage and declares silent marriage`() {
            val hand = createHandEntity(marriage = false)

            val handler = HandDeclareHandler(
                hand = HandModel(hand),
            )

            val guard = handler.canHandle(DeclarationOption.SILENT_MARRIAGE)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare SILENT_MARRIAGE when you are not having a marriage on hand.")

            assertThatThrownBy {
                handler.doHandle(DeclarationOption.SILENT_MARRIAGE)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Create' can not be performed: You can not declare SILENT_MARRIAGE when you are not having a marriage on hand.")
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