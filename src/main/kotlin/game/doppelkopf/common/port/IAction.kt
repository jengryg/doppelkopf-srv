package game.doppelkopf.common.port

import game.doppelkopf.security.UserDetails

/**
 * Simple top level interface for all actions used to initiate operations on the [game.doppelkopf.domain] implementation.
 * Actions are used to initiate the operational workflow in the domain engines.
 */
interface IAction {
    /**
     * Every command requires the presence of a [user] from the security context of the application.
     */
    val user: UserDetails
}