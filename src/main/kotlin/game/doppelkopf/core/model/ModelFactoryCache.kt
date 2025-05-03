package game.doppelkopf.core.model

import game.doppelkopf.common.IBaseModel
import game.doppelkopf.common.IBaseProperties
import java.util.*

/**
 * [MutableMap] based cache for the [game.doppelkopf.common.IModelFactory] implementations that identifies each
 * instance by the [IBaseProperties.id].
 */
class ModelFactoryCache<T : IBaseProperties, M : IBaseModel<T>> {
    private val instances = mutableMapOf<UUID, M>()

    /**
     * Returns the model [M] for the given [entity] from the [instances] cache if it is present and not `null`.
     * Otherwise, calls the [creator] function, puts its result into the [instances] cache and returns the call result.
     *
     * Note that the operation is not guaranteed to be atomic if the cache is being modified concurrently.
     */
    fun getOrPut(entity: T, creator: (e: T) -> M): M {
        return instances.getOrPut(entity.id) { creator(entity) }
    }
}