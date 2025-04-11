package game.doppelkopf.persistence.model

import game.doppelkopf.core.common.IBaseProperties
import java.util.*

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
     * @return [UUID.hashCode] of the [id]
     */
    override fun hashCode(): Int

    /**
     * @param other the object to compare to
     * @return true if and only if the [other] has the same underlying class and [id]
     */
    override fun equals(other: Any?): Boolean
}