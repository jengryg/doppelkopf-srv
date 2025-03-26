package game.doppelkopf.persistence

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

@MappedSuperclass
abstract class BaseEntity {
    /**
     * Application generated type 4 (pseudo random) [UUID] used as primary key.
     * This allows us to handle the primary key [id] inside our application as a non-nullable [UUID] property.
     *
     * This value should never be changed on an entity after it is constructed, but it is mutable to allow loading
     * of entities from database via ORM.
     */
    @Id
    @Column(length = 16, unique = true, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var id: UUID = UUID.randomUUID()

    /**
     * [version] is leveraged by the ORM to decide if the entity is already persisted or not.
     * Thus, we do not need to implement the [org.springframework.data.domain.Persistable] interface and can use
     * optimistic locking.
     */
    @Version
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var version: Long? = null

    /**
     * [created] is the [CreationTimestamp] automatically set and managed in the entity.
     * We always initialize it, to handle it as non-nullable [Instant] property.
     */
    @CreationTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var created: Instant = Instant.now()

    /**
     * [updated] is the [UpdateTimestamp] automatically set and managed in the entity.
     * We always initialize it, to handle it as non-nullable [Instant] property.
     */
    @UpdateTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var updated: Instant = Instant.now()

    /**
     * @return [UUID.hashCode] of the [id]
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }

    /**
     * @param other the object to compare to
     * @return true if and only if the [other] has the same underlying class and [id]
     */
    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            Hibernate.getClass(this) != Hibernate.getClass(other) -> false
            else -> (other as BaseEntity).id == id
        }
    }

    /**
     * @return simple string representation of the entity using the properties [id], [version], [created] and [updated]
     */
    override fun toString(): String {
        return "${this::class.simpleName} ${
            mapOf(
                "id" to id.toString(),
                "version" to version.toString(),
                "created" to created.toString(),
                "updated" to updated.toString()
            )
        }"
    }
}