package game.doppelkopf.core.model.game.handler

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.model.ModelFactoryProvider
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SeatingOrderResolverTest : BaseUnitTest() {
    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3])
    fun `get four players behind returns in circular increasing seat position order for 4 players`(given: Int) {
        val mfp = ModelFactoryProvider()

        val game = SeatingOrderResolver(
            entity = createGameEntity(),
            factoryProvider = mfp
        )

        // Players are not assumed to be in correct order in the game entity, thus (6 - it).
        // Player at index 3 is the one with the lowest seat number here.
        val players =
            List(4) { createPlayerEntity(seat = 6 - it, game = game.entity) }
                .map { mfp.player.create(it) }.onEach { game.addPlayer(it) }

        val quad = game.getFourPlayersBehind(players[given])
        val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
        // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
        // position order.
        assertThat(seatCompare.count { !it }).isEqualTo(
            when (given) {
                0 -> 0 // without circular breaking
                else -> 1 // In all other cases, we expect exactly one circular order break.
            }
        )

        // No player was let out in the selection
        assertThat(quad.toList().map { it.id }).containsExactlyInAnyOrderElementsOf(players.map { it.id })
        // Since we have 4 players, we expect the last one in the selection to be the given player.
        assertThat(quad.fourth).isEqualTo(players[given])
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4])
    fun `get four players behind returns in circular increasing seat position order for 5 players`(given: Int) {
        val mfp = ModelFactoryProvider()

        val game = SeatingOrderResolver(
            entity = createGameEntity(),
            factoryProvider = mfp
        )

        // Simplify test by assume players are already in correct order here.
        val players = List(5) { createPlayerEntity(seat = it, game = game.entity) }
            .map { mfp.player.create(it) }.onEach { game.addPlayer(it) }

        val quad = game.getFourPlayersBehind(players[given])
        val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
        // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
        // position order.
        assertThat(seatCompare.count { !it }).isEqualTo(
            when (given) {
                0, 4 -> 0 // without circular breaking
                else -> 1 // In all other cases, we expect exactly one circular order break.
            }
        )

        // Since we have more than 4 players, the given one is left out of the selection.
        assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5])
    fun `get four players behind returns in circular increasing seat position order for 6 players`(given: Int) {
        val mfp = ModelFactoryProvider()

        val game = SeatingOrderResolver(
            entity = createGameEntity(),
            factoryProvider = mfp
        )

        // Simplify test by assume players are already in correct order here.
        val players = List(6) { createPlayerEntity(seat = it, game = game.entity) }
            .map { mfp.player.create(it) }.onEach { game.addPlayer(it) }

        val quad = game.getFourPlayersBehind(players[given])
        val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
        // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
        // position order.
        assertThat(seatCompare.count { !it }).isEqualTo(
            when (given) {
                0, 1, 5 -> 0 // without circular breaking
                else -> 1 // In all other cases, we expect exactly one circular order break.
            }
        )

        // Since we have more than 4 players, the given one is left out of the selection.
        assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6])
    fun `get four players behind returns in circular increasing seat position order for 7 players`(given: Int) {
        val mfp = ModelFactoryProvider()

        val game = SeatingOrderResolver(
            entity = createGameEntity(),
            factoryProvider = mfp
        )

        // Simplify test by assume players are already in correct order here.
        val players = List(7) { createPlayerEntity(seat = it, game = game.entity) }
            .map { mfp.player.create(it) }.onEach { game.addPlayer(it) }

        val quad = game.getFourPlayersBehind(players[given])
        val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
        // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
        // position order.
        assertThat(seatCompare.count { !it }).isEqualTo(
            when (given) {
                0, 1, 2, 6 -> 0 // without circular breaking
                else -> 1 // In all other cases, we expect exactly one circular order break.
            }
        )
        // Since we have more than 4 players, the given one is left out of the selection.
        assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)

    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6, 7])
    fun `get four players behind returns in circular increasing seat position order for 8 players`(given: Int) {
        val mfp = ModelFactoryProvider()

        val game = SeatingOrderResolver(
            entity = createGameEntity(),
            factoryProvider = mfp
        )

        // Simplify test by assume players are already in correct order here.
        val players = List(8) { createPlayerEntity(seat = it, game = game.entity) }
            .map { mfp.player.create(it) }.onEach { game.addPlayer(it) }

        val quad = game.getFourPlayersBehind(players[given])
        val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
        // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
        // position order.
        assertThat(seatCompare.count { !it }).isEqualTo(
            when (given) {
                0, 1, 2, 3, 7 -> 0 // 0,1 without circular breaking
                else -> 1 // In all other cases, we expect exactly one circular order break.
            }
        )

        // Since we have more than 4 players, the given one is left out of the selection.
        assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)
    }

    @Test
    fun `get four players throws exception when given player is not at the game`() {
        val mfp = ModelFactoryProvider()

        val game = SeatingOrderResolver(
            entity = createGameEntity(),
            factoryProvider = mfp
        )

        List(4) { createPlayerEntity(seat = it, game = game.entity) }
            .map { mfp.player.create(it) }.onEach { game.addPlayer(it) }

        val notGamePlayer = mfp.player.create(
            entity = createPlayerEntity(seat = 17, game = game.entity)
        )

        assertThatThrownBy {
            game.getFourPlayersBehind(notGamePlayer)
        }.isInstanceOf(GameFailedException::class.java)
            .hasMessageContaining("Could not determine the position of given player PlayerEntity")
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3])
    fun `get four players behind fails when game has not at least 4 players`(playerCount: Int) {
        val mfp = ModelFactoryProvider()

        val game = SeatingOrderResolver(
            entity = createGameEntity(),
            factoryProvider = mfp
        )

        repeat(playerCount) {
            mfp.player.create(
                entity = createPlayerEntity(seat = it, game = game.entity)
            ).also { p -> game.addPlayer(p) }
        }

        assertThatThrownBy {
            game.getFourPlayersBehind(mockk())
        }.isInstanceOf(GameFailedException::class.java)
            .hasMessageContaining("Can not determine 4 players when game has only $playerCount players.")
    }
}