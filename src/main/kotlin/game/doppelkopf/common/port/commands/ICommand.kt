package game.doppelkopf.common.port.commands

import game.doppelkopf.security.UserDetails

interface ICommand {
    /**
     * Every command requires the presence of a [user] from the security context of the application.
     */
    val user: UserDetails

    /**
     * A simple and user-friendly short string describing the action of this command.
     * This value is shown to the users.
     */
    fun getSlug(): String
}