package game.doppelkopf.adapter.graphql.core.call

import game.doppelkopf.adapter.graphql.core.call.dto.Call
import game.doppelkopf.adapter.graphql.core.call.dto.MakeCallInput
import game.doppelkopf.adapter.persistence.model.call.CallPersistence
import game.doppelkopf.domain.hand.HandActionOrchestrator
import game.doppelkopf.domain.hand.ports.actions.HandActionCall
import game.doppelkopf.security.UserDetails
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class CallGraphQLController(
    private val callPersistence: CallPersistence,
    private val handActionOrchestrator: HandActionOrchestrator
) {
    @QueryMapping
    fun call(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Call {
        return Call(callPersistence.load(id), userDetails.entity)
    }

    @MutationMapping
    fun makeCall(
        @Argument @Valid makeCallInput: MakeCallInput,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Call {
        return handActionOrchestrator.execute(
            action = HandActionCall(
                user = userDetails,
                handId = makeCallInput.handId,
                callType = makeCallInput.callType
            )
        ).let {
            Call(it, userDetails.entity)
        }
    }
}