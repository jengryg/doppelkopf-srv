package game.doppelkopf.core.model

import game.doppelkopf.persistence.model.IBaseEntity

/**
 * [ModelAbstract] provides a basic delegation implementation for [equals], [hashCode] and [toString] to the underlying
 * [entity].
 */
abstract class ModelAbstract<T : IBaseEntity>(
    override val entity: T,
    protected val factoryProvider: ModelFactoryProvider
) : IBaseModel<T> {
    /**
     * Delegated to the underlying [entity] if [other] is also [ModelAbstract].
     *
     * @see IBaseEntity.equals
     */
    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is ModelAbstract<*> -> false
            else -> this.entity == other.entity
        }
    }

    /**
     * Delegated to the underlying [entity].
     */
    override fun hashCode(): Int {
        return entity.hashCode()
    }

    /**
     * Delegated to the underlying [entity].
     */
    override fun toString(): String {
        return entity.toString()
    }
}