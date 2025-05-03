package game.doppelkopf.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

/**
 * [IBaseEntityRepository] as custom [JpaRepository] extension for all [BaseEntity] derived entities.
 *
 * Must be annotated by [NoRepositoryBean] to prevent spring from creating an invalid repository of this interface.
 */
@NoRepositoryBean
interface IBaseEntityRepository<T : BaseEntity> : JpaRepository<T, UUID>