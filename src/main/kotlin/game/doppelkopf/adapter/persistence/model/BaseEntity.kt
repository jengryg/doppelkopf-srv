package game.doppelkopf.adapter.persistence.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.proxy.HibernateProxy
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
        /**
         * Get the [Class] of the given [obj] respecting the [HibernateProxy] without causing proxy initialization as
         * side effect.
         */
        fun clazzOf(obj: Any): Class<out Any>? = if (obj is HibernateProxy) {
            obj.hibernateLazyInitializer.persistentClass
        } else {
            obj.javaClass
        }

        return when {
            this === other -> true
            other == null -> false
            clazzOf(this) != clazzOf(other) -> false
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