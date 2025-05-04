package game.doppelkopf.common.model

interface IBaseModel<T : IBaseProperties> {
    /**
     * Holder for the underlying [IBaseProperties] derivative [T] for the property delegation.
     */
    val entity: T

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean

    override fun toString(): String
}