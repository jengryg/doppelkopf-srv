package game.doppelkopf.adapter.persistence.model

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

/**
 * [BaseEntity] as [MappedSuperclass] to simplify the implementation of [IBaseEntity].
 */
@MappedSuperclass
abstract class BaseEntity : IBaseEntity {
    @Id
    @Column(unique = true, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    override var id: UUID = UUID.randomUUID()

    @Version
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    override var version: Long? = null

    @CreationTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    override var created: Instant = Instant.now()

    @UpdateTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    override var updated: Instant = Instant.now()

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            Hibernate.getClass(this) != Hibernate.getClass(other) -> false
            else -> (other as BaseEntity).id == id
        }
    }

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