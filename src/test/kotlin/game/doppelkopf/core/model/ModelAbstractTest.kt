package game.doppelkopf.core.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.persistence.model.BaseEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ModelAbstractTest : BaseUnitTest() {

    /**
     * The simplest derivative of [BaseEntity] for the test.
     */
    class TestEntity : BaseEntity()

    /**
     * The simplest derivative of [ModelAbstract] using [TestEntity] for the test.
     */
    class TestModel(entity: TestEntity) : ModelAbstract<TestEntity>(entity, mockk())

    @Test
    fun `two instances created from the same underlying must be equal`() {
        val entity = TestEntity()

        val model = TestModel(entity)
        val other = TestModel(entity)

        assertThat(model == other).isTrue

        @Suppress("ReplaceCallWithBinaryOperator")
        assertThat(model.equals(other)).isTrue
    }

    @Test
    fun `two instances created from the same underlying must have the same hash code`() {
        val entity = TestEntity()

        val model = TestModel(entity)
        val other = TestModel(entity)

        assertThat(model.hashCode()).isEqualTo(other.hashCode())
    }

    @Test
    fun `two instances created from the same underlying must have the same string representation`() {
        val entity = TestEntity()

        val model = TestModel(entity)
        val other = TestModel(entity)

        assertThat(model.toString()).isEqualTo(other.toString())
    }
}