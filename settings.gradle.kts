rootProject.name = "doppelkopf"

dependencyResolutionManagement {
    /*
     * Centralize the repository declaration for the `buildSrc` using the (incubating) feature described in
     * https://docs.gradle.org/current/userguide/declaring_repositories.html#sub:centralized-repository-declaration
     */
    @Suppress("UnstableApiUsage")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}