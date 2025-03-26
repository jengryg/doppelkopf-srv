package game.doppelkopf.common.errors

import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity

/**
 * @return a [ResponseEntity] corresponding to this [ProblemDetail]
 */
fun ProblemDetail.toResponseEntity(): ResponseEntity<ProblemDetail> {
    return ResponseEntity.status(this.status).body(this)
}