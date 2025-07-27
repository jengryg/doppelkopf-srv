# Web Service for Doppelkopf Card Game
 
Backend implementation of the trick-taking card game Doppelkopf using Spring Boot and Kotlin for Java.

## Architecture Decisions Records

### Domain Layer and Persistence Adapter Entities are Coupled

The domain model implementations are relying on the persistence adapter entity classes and are not decoupled through
the introduction of further abstraction with interfaces.

This coupling is an intentional decision made to simplify and shorten the implementations of the persistence layer.

## Usage

Build the image locally via jib using docker inside the wsl.
Creates the image and pushes it to the registry, repository and tag configured in `gradle.properties` file.

````shell
wsl ./gradlew jib
````