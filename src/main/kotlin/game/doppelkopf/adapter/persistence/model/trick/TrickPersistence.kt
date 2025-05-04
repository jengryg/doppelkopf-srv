package game.doppelkopf.adapter.persistence.model.trick

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrickPersistence(
    private val trickRepository: TrickRepository
) {
    fun load(id: UUID): TrickEntity {
        return trickRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<TrickEntity>(id)
    }
}