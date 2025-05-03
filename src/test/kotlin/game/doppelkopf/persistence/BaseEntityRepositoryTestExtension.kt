package game.doppelkopf.persistence

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes

/**
 * Determine the name of the package containing the [IBaseEntityRepository] class.
 * This package is considered to be the root package of all [BaseEntity] and [IBaseEntityRepository] definitions of this
 * application.
 */
@Suppress("unused")
val persistenceRootPackageName: String by lazy {
    IBaseEntityRepository::class.qualifiedName
        ?.split(".")
        ?.takeWhile { it != IBaseEntityRepository::class.simpleName }
        ?.joinToString(".")
        ?: throw IllegalStateException("Could not determine the package of ${IBaseEntityRepository::class.qualifiedName}")
}

/**
 * Helper method to obtain the [KType] of the associated entity [T] of a [IBaseEntityRepository] instance.
 *
 * @return the [KType] of the entity [T]
 */
@Suppress("unused")
fun <T : BaseEntity> IBaseEntityRepository<T>.getEntityKType(): KType {
    return this::class.allSupertypes.first { it.classifier == IBaseEntityRepository::class }.arguments.singleOrNull()?.type
        ?: throw IllegalStateException("Could not determine the entity KType.")
}

/**
 * Helper method to obtain the [KClass] of the associated entity [T] of a [IBaseEntityRepository] instance.
 *
 * @return the [KClass] of the entity [T]
 */
@Suppress("unused")
fun <T : BaseEntity> IBaseEntityRepository<T>.getEntityKClass(): KClass<*> {
    return getEntityKType().classifier as? KClass<*>
        ?: throw IllegalStateException("Could not determine the entity KClass.")
}

/**
 * Helper method to obtain the [KType] of the repository implementing the [IBaseEntityRepository] interface.
 * This method can only be used on repositories that are only implementing the [IBaseEntityRepository] interface and
 * no other interface from the package named [persistenceRootPackageName], that is defined by the location of
 * [IBaseEntityRepository].
 *
 * @return the [KType] of this repository defined in package [persistenceRootPackageName] that inherits from
 * the [IBaseEntityRepository] interface
 */
@Suppress("unused")
fun <T : BaseEntity> IBaseEntityRepository<T>.getRepositoryKType(): KType {
    return this::class.allSupertypes.filter {
        (it.classifier as KClass<*>).qualifiedName?.startsWith(persistenceRootPackageName) == true
    }.singleOrNull { it.classifier != IBaseEntityRepository::class }
        ?: throw IllegalStateException("Could not determine the repository KType.")
}

/**
 * Helper method to obtain the [KClass] of the repository implementing the [IBaseEntityRepository] interface.
 * This method can only be used on repositories that are only implementing the [IBaseEntityRepository] interface and
 * no other interface from the package named [persistenceRootPackageName], that is defined by the location of
 * [IBaseEntityRepository].
 *
 * @return the [KClass] of this repository defined in package [persistenceRootPackageName] that inherits from
 * the [IBaseEntityRepository] interface
 */
@Suppress("unused")
fun <T : BaseEntity> IBaseEntityRepository<T>.getRepositoryKClass(): KClass<*> {
    return getRepositoryKType().classifier as? KClass<*>
        ?: throw IllegalStateException("Could not determine the repository KClass.")
}