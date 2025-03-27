package game.doppelkopf.persistence

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.reflect.KClass

class EntityNotFoundException(
    kClass: KClass<*>,
    id: UUID? = null,
    cause: Throwable? = null
) : ApplicationRuntimeException(
    HttpStatus.NOT_FOUND,
    cause
) {
    init {
        setTitle("Entity not found")
        if (id != null) {
            setDetail("The entity of type ${kClass.simpleName} with id $id was not found.")
        } else {
            setDetail("the entity of type ${kClass.simpleName} was not found with the applied search criteria.")
        }
    }

    companion object {
        /**
         * Create [EntityNotFoundException] with the given [E] class set as class parameter and the optional [id] and [cause]
         * parameters.
         *
         * @param E the class of the [BaseEntity] used as reified type param
         *
         * @param id the [UUID] of the entity that was not found or `null` if no uuid should be included in the message, or it
         * is unknown.
         *
         * @param cause The cause of this [EntityNotFoundException] or `null` if the cause is nonexistent, i.e.
         * (no previous [Throwable]) or it is unknown.
         *
         * @return the [EntityNotFoundException] constructed from the given parameters
         */
        inline fun <reified E : BaseEntity> forEntity(
            id: UUID? = null,
            cause: Throwable? = null
        ): EntityNotFoundException {
            return EntityNotFoundException(
                kClass = E::class,
                id = id,
                cause = cause
            )
        }
    }
}

