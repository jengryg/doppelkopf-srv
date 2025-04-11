package game.doppelkopf.core.model.user

import game.doppelkopf.core.common.IBaseProperties

interface IUserProperties : IBaseProperties {
    val username: String

    // Note: We are not defining the properties used for the Spring Security integration here, since we do not want to
    // automatically delegate these properties. Especially, do not specify the password property here.
}