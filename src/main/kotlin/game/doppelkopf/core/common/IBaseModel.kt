package game.doppelkopf.core.common

interface IBaseModel<T : IBaseProperties> {
    /**
     * Holder for the underlying [IBaseProperties] derivative [T] for the property delegation.
     */
    val entity: T
}