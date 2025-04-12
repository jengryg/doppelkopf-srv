package game.doppelkopf.core.model

import game.doppelkopf.persistence.model.IBaseEntity

interface IBaseModel<T : IBaseEntity> {
    /**
     * Holder for the underlying [IBaseEntity] derivative [T] for the property delegation.
     */
    val entity: T

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean

    override fun toString(): String
}