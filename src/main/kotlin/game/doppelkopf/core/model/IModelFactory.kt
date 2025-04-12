package game.doppelkopf.core.model

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.persistence.model.BaseEntity

/**
 * For each [IBaseModel] and corresponding [BaseEntity], a factory implementing this interface should be introduced to
 * centralize all initial conversions from entity to model in the application.
 *
 * Only the simple [create] consuming a single [T] and producing a single [M] must be implemented.
 * Overloads to map [List] and [Set] are automatically provided through this interface.
 * The [create] from [IBaseModel] entity un-wrapper is also provided automatically.
 */
interface IModelFactory<T : BaseEntity, out M : IBaseModel<T>> {
    /**
     * Use the given [entity] to initialize a new instance of model [M].
     */
    fun create(entity: T): M

    /**
     * Use the given [model] to initialize a new instance of model [M].
     * The [IBaseModel.entity] is injected into the [create] method.
     */
    fun create(model: IBaseModel<T>): M {
        return create(model.entity)
    }

    /**
     * Overload to apply [create] to each element of the list via [map].
     */
    fun create(entityList: List<T>): List<M> {
        return entityList.map { create(it) }
    }

    /**
     * Overload to apply [create] to each element of the set via [map].
     */
    fun create(entitySet: Set<T>): Set<M> {
        return entitySet.map { create(it) }.toSet()
    }
}