package game.doppelkopf

/**
 * All [org.springframework.core.env.Profiles] that are used for this service should be defined as constants here.
 */
object SpringProfiles {
    /**
     * This profile is set via system property in gradle whenever tests are executed or when bootTestRun is used.
     */
    const val TEST = "test"

    /**
     * This profile should be used for default local runs of the application on developer machines.
     */
    const val LOCAL = "local"

    /**
     * This profile should be used for temporary local runs of the application on developer machines.
     */
    const val TEMP = "temp"
}