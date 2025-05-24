package game.doppelkopf.domain.hand.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.enums.Declaration
import game.doppelkopf.domain.hand.enums.DeclarationOption
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class HandDeclareModelTest : BaseUnitTest() {
    @Test
    fun `declare healthy when hand does not have marriage updates hand`() {
        val mfp = ModelFactoryProvider()

        val hand = HandDeclareModel(
            entity = createHandEntity(hasMarriage = false),
            factoryProvider = mfp
        )

        val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
        assertThat(guard.isSuccess).isTrue

        hand.declare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)

        assertThat(hand.declared).isEqualTo(Declaration.HEALTHY)
    }

    @Test
    fun `declare silent marriage when hand has marriage updates hand`() {
        val mfp = ModelFactoryProvider()

        val hand = HandDeclareModel(
            entity = createHandEntity(hasMarriage = true),
            factoryProvider = mfp
        )

        val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)
        assertThat(guard.isSuccess).isTrue

        hand.declare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)

        assertThat(hand.declared).isEqualTo(Declaration.SILENT_MARRIAGE)
    }

    // @Disabled("Until solo system is implemented, declaring reservation without a marriage on hand will lead to a failed state.")
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `declare RESERVATION updates hand regardless of marriage status`(hasMarriage: Boolean) {
        val mfp = ModelFactoryProvider()

        val hand = HandDeclareModel(
            entity = createHandEntity(hasMarriage = hasMarriage),
            factoryProvider = mfp
        )

        val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.RESERVATION)
        assertThat(guard.isSuccess).isTrue

        hand.declare(user = hand.player.user, declarationOption = DeclarationOption.RESERVATION)

        assertThat(hand.declared).isEqualTo(Declaration.RESERVATION)
    }

    @ParameterizedTest
    @EnumSource(DeclarationOption::class)
    fun `guard yields exception when user is not owner of hand`(declarationOption: DeclarationOption) {
        val mfp = ModelFactoryProvider()

        val hand = HandDeclareModel(
            entity = createHandEntity(hasMarriage = false),
            factoryProvider = mfp
        )
        val user = mfp.user.create(entity = createUserEntity())

        val guard = hand.canDeclare(user = user, declarationOption = declarationOption)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: You can only declare on your own hand.")

        assertThatThrownBy {
            hand.declare(user = user, declarationOption = declarationOption)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: You can only declare on your own hand.")
    }

    @ParameterizedTest
    @EnumSource(Declaration::class, names = ["NOTHING"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when hand has already declared`(declaration: Declaration) {
        val mfp = ModelFactoryProvider()

        val hand = HandDeclareModel(
            entity = createHandEntity(
                declaration = declaration,
                hasMarriage = false
            ),
            factoryProvider = mfp
        )

        val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: This hand has already made a declaration.")

        assertThatThrownBy {
            hand.declare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: This hand has already made a declaration.")
    }

    @Test
    fun `guard yields exception when hand has marriage and declares healthy`() {
        val mfp = ModelFactoryProvider()

        val hand = HandDeclareModel(
            entity = createHandEntity(hasMarriage = true),
            factoryProvider = mfp
        )

        val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: You can not declare HEALTHY when you have a marriage on hand.")

        assertThatThrownBy {
            hand.declare(user = hand.player.user, declarationOption = DeclarationOption.HEALTHY)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: You can not declare HEALTHY when you have a marriage on hand.")
    }

    @Test
    fun `guard yields exception when hand has no marriage and declares silent marriage`() {
        val mfp = ModelFactoryProvider()

        val hand = HandDeclareModel(
            entity = createHandEntity(hasMarriage = false),
            factoryProvider = mfp
        )

        val guard = hand.canDeclare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: You can not declare SILENT_MARRIAGE when you are not having a marriage on hand.")

        assertThatThrownBy {
            hand.declare(user = hand.player.user, declarationOption = DeclarationOption.SILENT_MARRIAGE)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: You can not declare SILENT_MARRIAGE when you are not having a marriage on hand.")
    }
}