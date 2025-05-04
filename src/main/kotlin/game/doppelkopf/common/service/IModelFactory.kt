package game.doppelkopf.common.service

import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.common.model.IBaseProperties
import game.doppelkopf.utils.Quadruple

/**
 * For each [IBaseModel] and corresponding [IBaseProperties] implementation in the persistence layer, a factory for the
 * creation of model instances can be implemented using this interface.
 *
 * Only the [create] method must be implemented. Other methods are provided with a corresponding default implementation
 * in this interface.
 */
interface IModelFactory<T : IBaseProperties, M : IBaseModel<T>> {
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