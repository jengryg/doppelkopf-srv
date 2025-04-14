package game.doppelkopf.persistence

import game.doppelkopf.persistence.model.BaseEntity
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes

/**
 * Determine the name of the package containing the [BaseEntityRepository] class.
 * This package is considered to be the root package of all [BaseEntity] and [BaseEntityRepository] definitions of this
 * application.
 */
val persistenceRootPackageName: String by lazy {
    BaseEntityRepository::class.qualifiedName
        ?.split(".")
        ?.takeWhile { it != BaseEntityRepository::class.simpleName }
        ?.joinToString(".")
        ?: throw IllegalStateException("Could not determine the package of ${BaseEntityRepository::class.qualifiedName}")
}

/**
 * Helper method to obtain the [KType] of the associated entity [T] of a [BaseEntityRepository] instance.
 *
 * @return the [KType] of the entity [T]
 */
@Suppress("unused")
fun <T : BaseEntity> BaseEntityRepository<T>.getEntityKType(): KType {
    return this::class.allSupertypes.first { it.classifier == BaseEntityRepository::class }.arguments.singleOrNull()?.type
        ?: throw IllegalStateException("Could not determine the entity KType.")
}

/**
 * Helper method to obtain the [KClass] of the associated entity [T] of a [BaseEntityRepository] instance.
 *
 * @return the [KClass] of the entity [T]
 */
@Suppress("unused")
fun <T : BaseEntity> BaseEntityRepository<T>.getEntityKClass(): KClass<*> {
    return getEntityKType().classifier as? KClass<*>
        ?: throw IllegalStateException("Could not determine the entity KClass.")
}

/**
 * Helper method to obtain the [KType] of the repository implementing the [BaseEntityRepository] interface.
 * This method can only be used on repositories that are only implementing the [BaseEntityRepository] interface and
 * no other interface from the package named [persistenceRootPackageName], that is defined by the location of
 * [BaseEntityRepository].
 *
 * @return the [KType] of this repository defined in package [persistenceRootPackageName] that inherits from
 * the [BaseEntityRepository] interface
 */
@Suppress("unused")
fun <T : BaseEntity> BaseEntityRepository<T>.getRepositoryKType(): KType {
    return this::class.allSupertypes.filter {
        (it.classifier as KClass<*>).qualifiedName?.startsWith(persistenceRootPackageName) == true
    }.singleOrNull { it.classifier != BaseEntityRepository::class }
        ?: throw IllegalStateException("Could not determine the repository KType.")
}

/**
 * Helper method to obtain the [KClass] of the repository implementing the [BaseEntityRepository] interface.
 * This method can only be used on repositories that are only implementing the [BaseEntityRepository] interface and
 * no other interface from the package named [persistenceRootPackageName], that is defined by the location of
 * [BaseEntityRepository].
 *
 * @return the [KClass] of this repository defined in package [persistenceRootPackageName] that inherits from
 * the [BaseEntityRepository] interface
 */
fun <T : BaseEntity> BaseEntityRepository<T>.getRepositoryKClass(): KClass<*> {
    return getRepositoryKType().classifier as? KClass<*>
        ?: throw IllegalStateException("Could not determine the repository KClass.")
}