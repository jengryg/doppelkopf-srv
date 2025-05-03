package game.doppelkopf.persistence

import game.doppelkopf.common.IBaseProperties

/**
 * [IBaseEntity] defines the minimum properties we require for an entity that allows persistence of an [IBaseProperties]
 * model object.
 */
interface IBaseEntity : IBaseProperties {
    /**
     * [version] is leveraged by the ORM to decide if the entity is already persisted or not.
     * Thus, we do not need to implement the [org.springframework.data.domain.Persistable] interface and can use
     * optimistic locking.
     */
    var version: Long?

    /**
     * Implement [hashCode] based on the [id] of the implementing class using [java.util.UUID.hashCode].
     */
    override fun hashCode(): Int

    /**
     * Implement [equals] using [org.hibernate.Hibernate.getClass] to check the underlying classes of proxies.
     * Finally, check the underlying [id].
     */
    override fun equals(other: Any?): Boolean

    override fun toString(): String
}