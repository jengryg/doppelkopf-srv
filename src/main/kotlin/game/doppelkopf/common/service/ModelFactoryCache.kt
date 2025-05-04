package game.doppelkopf.common.service

import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.common.model.IBaseProperties
import java.util.UUID

/**
 * [MutableMap] based cache for the [IModelFactory] implementations that identifies each
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