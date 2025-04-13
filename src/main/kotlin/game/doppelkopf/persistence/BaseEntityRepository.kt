package game.doppelkopf.persistence

import game.doppelkopf.persistence.model.BaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

/**
 * [BaseEntityRepository] as custom [JpaRepository] extension for all [BaseEntity] derived entities.
 *
 * Must be annotated by [NoRepositoryBean] to prevent spring from creating an invalid repository of this interface.
 */
@NoRepositoryBean
interface BaseEntityRepository<T : BaseEntity> : JpaRepository<T, UUID>