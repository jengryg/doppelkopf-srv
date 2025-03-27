package game.doppelkopf.errors

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.ProblemDetail

/**
 * Use this class if you want to parse [ProblemDetail] responses from the API during testing.
 * Properties in the json response that are unknown on [ProblemDetail] are ignored during parsing.
 *
 * This implementation also defines the optional [Any] typed property [errors] to collect the additional data this
 * application uses to include further information for specific errors.
 */
@Suppress("MemberVisibilityCanBePrivate")
@JsonIgnoreProperties(ignoreUnknown = true)
class ProblemDetailResponse : ProblemDetail() {
    /**
     * Extension Member to ProblemDetails to handle additional data in the response.
     * See https://www.rfc-editor.org/rfc/rfc9457.html#name-extension-members for more information on extension members.
     */
    var errors: Any? = null
}