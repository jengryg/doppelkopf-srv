package game.doppelkopf.core.model

import game.doppelkopf.persistence.model.BaseEntity
import game.doppelkopf.utils.Quadruple

/**
 * For each [IBaseModel] and corresponding [BaseEntity], a companion object extending this interface can be implemented
 * to provide standard methods for the creation of model instances.
 *
 * Only the [create] method must be implemented. Other methods are provided with a corresponding default implementation
 * in this interface.
 */
interface IModelFactory<T : BaseEntity, M : IBaseModel<T>> {
    /**
     * Use the given [entity] to get an instance of model [M] from the
     */
    fun create(entity: T): M

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

    /**
     * Overload to apply [create] to each element of the [Quadruple] via [Quadruple.map].
     */
    fun create(entityQuadruple: Quadruple<T>): Quadruple<M> {
        return entityQuadruple.map { create(it) }
    }
}