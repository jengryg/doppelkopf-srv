package game.doppelkopf.common

import game.doppelkopf.security.UserDetails

interface ICommand {
    /**
     * Every command requires the presence of a [user] from the security context of the application.
     */
    val user: UserDetails
}