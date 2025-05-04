package game.doppelkopf.common.model

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

/**
 * [IBaseProperties] defines the minimum properties we require for a model object.
 */
interface IBaseProperties {
    /**
     * Application generated type 4 (pseudo random) [UUID] used as primary key.
     * This allows us to handle the primary key [id] inside our application as a non-nullable [UUID] property.
     *
     * This value should never be changed on an entity after it is constructed, but it is mutable to allow loading
     * of entities from database via ORM.
     */
    var id: UUID

    /**
     * [created] is the [CreationTimestamp] automatically set and managed in the entity.
     * We always initialize it, to handle it as non-nullable [Instant] property.
     */
    var created: Instant

    /**
     * [updated] is the [UpdateTimestamp] automatically set and managed in the entity.
     * We always initialize it, to handle it as non-nullable [Instant] property.
     */
    var updated: Instant
}