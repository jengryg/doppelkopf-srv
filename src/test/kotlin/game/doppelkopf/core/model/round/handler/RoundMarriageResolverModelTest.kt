package game.doppelkopf.core.model.round.handler

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.ModelFactoryProvider
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoundMarriageResolverModelTest : BaseUnitTest() {
    @Test
    fun `not resolved after third trick configures solo round for married hand`() {
        val entity = createRoundEntity().apply {
            contract = RoundContract.MARRIAGE_UNRESOLVED

            repeat(4) {
                hands.add(createHandEntity(index = it, hasMarriage = 2 == it))
            }

            repeat(3) {
                tricks.add(
                    createTrickEntity(round = this, number = it + 1).apply {
                        winner = hands.single { h -> h.index == 2 }
                    }
                )
            }
        }

        val mfp = ModelFactoryProvider()

        val model = RoundMarriageResolverModel(entity, mfp)

        assertThatCode {
            model.resolveMarriage()
        }.doesNotThrowAnyException()

        val teamRE = entity.hands.filter { it.index == 2 }
        val teamKO = entity.hands.filter { it.index != 2 }

        assertThat(model.contract).isEqualTo(RoundContract.MARRIAGE_SOLO)
        assertThat(teamRE).hasSize(1)
        assertThat(teamKO).hasSize(3)

        teamRE.forEach {
            assertThat(it.isMarried).isFalse
            assertThat(it.playsSolo).isTrue
            assertThat(it.internalTeam).isEqualTo(Team.RE)
            assertThat(it.playerTeam).isEqualTo(Team.RE)
            assertThat(it.publicTeam).isEqualTo(Team.RE)
        }

        teamKO.forEach {
            assertThat(it.isMarried).isFalse
            assertThat(it.internalTeam).isEqualTo(Team.KO)
            assertThat(it.playerTeam).isEqualTo(Team.KO)
            assertThat(it.publicTeam).isEqualTo(Team.KO)
        }
    }

    @Test
    fun `resolving marries hands when trick is won by not marriage hand`() {
        val entity = createRoundEntity().apply {
            contract = RoundContract.MARRIAGE_UNRESOLVED

            repeat(4) {
                hands.add(createHandEntity(index = it, hasMarriage = 2 == it))
            }

            tricks.add(
                createTrickEntity(round = this).apply {
                    winner = hands.single { it.index == 1 }
                }
            )
        }

        val mfp = ModelFactoryProvider()

        val model = RoundMarriageResolverModel(entity, mfp)

        assertThatCode {
            model.resolveMarriage()
        }.doesNotThrowAnyException()

        val teamRE = entity.hands.filter { it.index == 1 || it.index == 2 }
        val teamKO = entity.hands.filter { it.index != 1 && it.index != 2 }

        assertThat(model.contract).isEqualTo(RoundContract.MARRIAGE_RESOLVED)
        assertThat(teamRE).hasSize(2)
        assertThat(teamKO).hasSize(2)

        teamRE.forEach {
            assertThat(it.isMarried).isTrue
            assertThat(it.internalTeam).isEqualTo(Team.RE)
            assertThat(it.playerTeam).isEqualTo(Team.RE)
            assertThat(it.publicTeam).isEqualTo(Team.RE)
        }

        teamKO.forEach {
            assertThat(it.isMarried).isFalse
            assertThat(it.internalTeam).isEqualTo(Team.KO)
            assertThat(it.playerTeam).isEqualTo(Team.KO)
            assertThat(it.publicTeam).isEqualTo(Team.KO)
        }
    }

    @Test
    fun `resolving skips when trick is won by marriage hand and limit is not reached`() {
        val entity = createRoundEntity().apply {
            contract = RoundContract.MARRIAGE_UNRESOLVED

            repeat(4) {
                hands.add(createHandEntity(index = it, hasMarriage = 2 == it))
            }

            tricks.add(
                createTrickEntity(round = this).apply {
                    winner = hands.single { it.hasMarriage }
                }
            )
        }

        val mfp = ModelFactoryProvider()

        val model = RoundMarriageResolverModel(entity, mfp)

        assertThatCode {
            model.resolveMarriage()
        }.doesNotThrowAnyException()

        assertThat(model.contract).isEqualTo(RoundContract.MARRIAGE_UNRESOLVED)
    }

    @Test
    fun `resolving fails game when no marriage hand is found`() {
        val entity = createRoundEntity().apply {
            contract = RoundContract.MARRIAGE_UNRESOLVED

            repeat(4) {
                hands.add(createHandEntity(index = it, hasMarriage = false))
            }

            tricks.add(
                createTrickEntity(round = this).apply {
                    winner = hands.single { it.index == 0 }
                }
            )
        }

        val mfp = ModelFactoryProvider()

        val model = RoundMarriageResolverModel(entity, mfp)

        assertThatThrownBy {
            model.resolveMarriage()
        }.isInstanceOf(GameFailedException::class.java)
            .hasMessageContaining("Could not determine the hand with the marriage.")
    }

    @ParameterizedTest
    @EnumSource(RoundContract::class, names = ["MARRIAGE_UNRESOLVED"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception on wrong round contract`(roundContract: RoundContract) {
        val entity = createRoundEntity().apply {
            contract = roundContract
        }

        val mfp = ModelFactoryProvider()

        val model = RoundMarriageResolverModel(entity, mfp)

        val guard = model.canResolveMarriage()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Marriage:Resolve' can not be performed: Only rounds that have the contract MARRIAGE_UNRESOLVED can resolve a marriage.")

        assertThatThrownBy {
            model.resolveMarriage()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Marriage:Resolve' can not be performed: Only rounds that have the contract MARRIAGE_UNRESOLVED can resolve a marriage.")
    }

    @Test
    fun `guard yields exception when round has no current trick`() {
        val entity = createRoundEntity().apply {
            contract = RoundContract.MARRIAGE_UNRESOLVED
        }

        val mfp = ModelFactoryProvider()

        val model = RoundMarriageResolverModel(entity, mfp)

        val guard = model.canResolveMarriage()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Marriage:Resolve' can not be performed: Could not determine the last trick of the round")

        assertThatThrownBy {
            model.resolveMarriage()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Marriage:Resolve' can not be performed: Could not determine the last trick of the round")
    }

    @Test
    fun `guard yields exception when current trick of round has no winner`() {
        val entity = createRoundEntity().apply {
            contract = RoundContract.MARRIAGE_UNRESOLVED

            tricks.add(createTrickEntity(round = this).apply { winner = null })
        }

        val mfp = ModelFactoryProvider()

        val model = RoundMarriageResolverModel(entity, mfp)

        val guard = model.canResolveMarriage()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Marriage:Resolve' can not be performed: There is no winner in the current trick")

        assertThatThrownBy {
            model.resolveMarriage()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Marriage:Resolve' can not be performed: There is no winner in the current trick")
    }
}