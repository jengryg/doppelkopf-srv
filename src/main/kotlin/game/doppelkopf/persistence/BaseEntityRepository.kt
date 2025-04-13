package game.doppelkopf.persistence

import game.doppelkopf.persistence.model.BaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * [BaseEntityRepository] as custom [JpaRepository] extension for all [BaseEntity] derived entities.
 */
interface BaseEntityRepository<T : BaseEntity> : JpaRepository<T, UUID>