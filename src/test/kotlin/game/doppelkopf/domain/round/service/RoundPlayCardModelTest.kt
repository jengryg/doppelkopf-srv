package game.doppelkopf.domain.round.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.hand.service.HandCardPlayModel
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.trick.service.TrickCardPlayModel
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.Test

class RoundPlayCardModelTest : BaseUnitTest() {
    @Test
    fun `play the first card of the first trick as opening player succeeds`() {
        mockkConstructor(HandCardPlayModel::class)
        every {
            anyConstructed<HandCardPlayModel>().playCard(any(), any())
        } returns Result.success(Unit)

        mockkConstructor(TrickCardPlayModel::class)
        every {
            anyConstructed<TrickCardPlayModel>().playCard(any())
        } just Runs

        val mfp = ModelFactoryProvider()

        val round = createRound()

        val user = mfp.user.create(round.dealer.user)

        val model = RoundPlayCardModel(
            entity = round,
            factoryProvider = mfp
        )

        val guard = model.canPlayCard(user)
        assertThat(guard.isSuccess).isTrue

        val card = Deck.create(round.deckMode).getCard("QC0").getOrThrow()
        val (trick, turn) = model.playCard(card, user)

        assertThat(trick.round.id).isEqualTo(round.id)
        assertThat(trick.number).isEqualTo(1)
        assertThat(trick.openIndex).isEqualTo(0)
        assertThat(trick.demand).isEqualTo(CardDemand.COLORED)

        assertThat(turn.number).isEqualTo(1)
        assertThat(turn.round.id).isEqualTo(round.id)
        assertThat(turn.hand.id).isEqualTo(round.hands.single { it.player.user.id == user.id }.id)
        assertThat(turn.trick.id).isEqualTo(trick.id)

        verify(exactly = 1) {
            anyConstructed<HandCardPlayModel>().playCard(any(), any())
            anyConstructed<TrickCardPlayModel>().playCard(any())
        }
    }

    @Test
    fun `play the first card as the winner of the previous trick succeeds`() {
        mockkConstructor(HandCardPlayModel::class)
        every {
            anyConstructed<HandCardPlayModel>().playCard(any(), any())
        } returns Result.success(Unit)

        mockkConstructor(TrickCardPlayModel::class)
        every {
            anyConstructed<TrickCardPlayModel>().playCard(any())
        } just Runs

        val mfp = ModelFactoryProvider()

        val round = createRound()
        val existingTrick = createTrickEntity(round = round).apply {
            winner = round.hands.first()
        }
        round.tricks.add(existingTrick)
        repeat(4) {
            round.turns.add(
                createTurnEntity(round = round)
            )
        }


        val model = RoundPlayCardModel(
            entity = round,
            factoryProvider = mfp
        )

        val winner = mfp.user.create(round.hands.first().player.user)

        val guard = model.canPlayCard(winner)
        assertThat(guard.isSuccess).isTrue

        val card = Deck.create(round.deckMode).getCard("QC0").getOrThrow()
        val (trick, turn) = model.playCard(card, winner)

        assertThat(trick.id).isNotEqualTo(existingTrick.id)

        assertThat(trick.round.id).isEqualTo(round.id)
        assertThat(trick.number).isEqualTo(2)
        assertThat(trick.openIndex).isEqualTo(round.hands.first().index)

        assertThat(turn.number).isEqualTo(5)
        assertThat(turn.round.id).isEqualTo(round.id)
        assertThat(turn.hand.id).isEqualTo(round.hands.single { it.player.user.id == winner.id }.id)
        assertThat(turn.trick.id).isEqualTo(trick.id)


        verify(exactly = 1) {
            anyConstructed<HandCardPlayModel>().playCard(any(), any())
            anyConstructed<TrickCardPlayModel>().playCard(any())
        }
    }

    @Test
    fun `play a card into the trick when it is your turn succeeds`() {
        mockkConstructor(HandCardPlayModel::class)
        every {
            anyConstructed<HandCardPlayModel>().playCard(any(), any())
        } returns Result.success(Unit)

        mockkConstructor(TrickCardPlayModel::class)
        every {
            anyConstructed<TrickCardPlayModel>().playCard(any())
        } just Runs

        val mfp = ModelFactoryProvider()

        val round = createRound()
        val existingTrick = createTrickEntity(round = round, openIndex = 2, number = 1).apply {
            cards.add("AS0")
            cards.add("AS1")
            cards.add("TS0")
        }
        round.tricks.add(existingTrick)
        repeat(3) {
            round.turns.add(
                createTurnEntity(round = round)
            )
        }

        val nextTurn = mfp.user.create(round.hands.single { it.index == 1 }.player.user)

        val model = RoundPlayCardModel(
            entity = round,
            factoryProvider = mfp
        )

        val guard = model.canPlayCard(nextTurn)
        assertThat(guard.isSuccess).isTrue

        val card = Deck.create(round.deckMode).getCard("QC0").getOrThrow()
        val (trick, turn) = model.playCard(card, nextTurn)

        assertThat(trick.id).isEqualTo(existingTrick.id)

        assertThat(trick.round.id).isEqualTo(round.id)
        assertThat(trick.number).isEqualTo(1)
        assertThat(trick.openIndex).isEqualTo(2)

        assertThat(turn.number).isEqualTo(4)
        assertThat(turn.round.id).isEqualTo(round.id)
        assertThat(turn.hand.id).isEqualTo(round.hands.single { it.index == 1 }.id)
        assertThat(turn.trick.id).isEqualTo(trick.id)
    }

    @ParameterizedTest
    @EnumSource(RoundState::class, names = ["PLAYING_TRICKS"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when round is not in correct state`(roundState: RoundState) {
        val mfp = ModelFactoryProvider()

        val model = RoundPlayCardModel(
            entity = createRoundEntity().apply { state = roundState },
            factoryProvider = mfp
        )

        val user = model.dealer.user

        val guard = model.canPlayCard(user)
        assertThat(guard.isFailure).isTrue

        val card = model.deck.getCard("QC0").getOrThrow()

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The round must be in PLAYING_TRICKS state to open a new trick.")

        assertThatThrownBy {
            model.playCard(card, model.dealer.user)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The round must be in PLAYING_TRICKS state to open a new trick.")
    }

    @Test
    fun `guard yields exception when user has no hand in the round`() {
        val mfp = ModelFactoryProvider()

        val model = RoundPlayCardModel(
            entity = createRoundEntity().apply { state = RoundState.PLAYING_TRICKS },
            factoryProvider = mfp
        )

        val user = model.dealer.user

        val guard = model.canPlayCard(user)
        assertThat(guard.isFailure).isTrue

        val card = model.deck.getCard("QC0").getOrThrow()

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: You are not playing in this round.")

        assertThatThrownBy {
            model.playCard(card, model.dealer.user)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: You are not playing in this round.")
    }

    @Test
    fun `guard yields exception when user is not the one opening the first trick`() {
        val mfp = ModelFactoryProvider()

        val round = createRound()

        val user = mfp.user.create(round.hands.last().player.user)

        val model = RoundPlayCardModel(
            entity = round,
            factoryProvider = mfp
        )

        val guard = model.canPlayCard(user)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: Only the player directly behind the dealer can open the first trick of the round.")

        val card = model.deck.getCard("QC0").getOrThrow()

        assertThatThrownBy {
            model.playCard(card, user)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: Only the player directly behind the dealer can open the first trick of the round.")
    }

    @Test
    fun `guard yields exception when user is not the winner of the previous trick opening the next trick`() {
        val mfp = ModelFactoryProvider()

        val round = createRound()
        round.tricks.add(
            createTrickEntity(round = round).apply {
                winner = round.hands.first()
            }
        )

        val model = RoundPlayCardModel(
            entity = round,
            factoryProvider = mfp
        )

        val notWinner = mfp.user.create(round.hands.last().player.user)

        val guard = model.canPlayCard(notWinner)
        assertThat(guard.isFailure).isTrue
        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: Only the winner of the previous trick can open the next trick of the round.")

        val card = model.deck.getCard("QC0").getOrThrow()

        assertThatThrownBy {
            model.playCard(card, notWinner)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: Only the winner of the previous trick can open the next trick of the round.")
    }

    @Test
    fun `guard yields exception when it is not the turn of the user`() {
        val mfp = ModelFactoryProvider()

        val round = createRound()
        round.tricks.add(
            createTrickEntity(round = round, openIndex = 2).apply {
                cards.add("AS0")
                cards.add("AS1")
                cards.add("TS0")
            }
        )
        val notTurn = mfp.user.create(round.hands.first { it.index == 2 }.player.user)

        val model = RoundPlayCardModel(
            entity = round,
            factoryProvider = mfp
        )

        val guard = model.canPlayCard(notTurn)
        assertThat(guard.isFailure).isTrue
        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: It is not your turn to play a card.")

        val card = model.deck.getCard("QC0").getOrThrow()

        assertThatThrownBy {
            model.playCard(card, notTurn)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: It is not your turn to play a card.")
    }

    private fun createRound(): RoundEntity {
        val userEntities = List(4) { createUserEntity() }
        val handEntities = userEntities.mapIndexed { index, user ->
            createHandEntity(player = createPlayerEntity(user = user), index = index, cards = mutableListOf("QC0"))
        }

        return createRoundEntity(dealer = handEntities.first().player).apply {
            state = RoundState.PLAYING_TRICKS

            hands.addAll(handEntities)
        }
    }
}