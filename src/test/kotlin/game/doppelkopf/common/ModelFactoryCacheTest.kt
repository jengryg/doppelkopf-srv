package game.doppelkopf.common

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.adapter.persistence.model.BaseEntity
import game.doppelkopf.common.service.ModelFactoryCache
import game.doppelkopf.domain.ModelAbstract
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ModelFactoryCacheTest : BaseUnitTest() {
    /**
     * The simplest derivative of [BaseEntity] for the test.
     */
    class TestEntity : BaseEntity()

    /**
     * The simplest derivative of [ModelAbstract] using [TestEntity] for the test.
     */
    class TestModel(entity: TestEntity) : ModelAbstract<TestEntity>(entity, mockk())

    @Test
    fun `cache instantiates when id of entity unknown`() {
        val cache = ModelFactoryCache<TestEntity, TestModel>()
        val entities = List(42) { TestEntity() }

        var counter = 0
        entities.map { e ->
            cache.getOrPut(e) {
                counter++
                TestModel(it)
            }
        }

        Assertions.assertThat(counter).isEqualTo(42)
    }

    @Test
    fun `cache returns when id of entity is known`() {
        val cache = ModelFactoryCache<TestEntity, TestModel>()
        val entity = TestEntity()

        var counter = 0
        val model = cache.getOrPut(entity) {
            counter++
            TestModel(it)
        }
        val other = cache.getOrPut(entity) {
            counter++
            TestModel(it)
        }

        Assertions.assertThat(counter).isEqualTo(1)
        Assertions.assertThat(model === other).isTrue
    }
}