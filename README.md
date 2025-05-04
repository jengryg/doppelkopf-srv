# Web Service for Doppelkopf Card Game

## Architecture Decisions Records

### Domain Layer and Persistence Adapter Entities are Coupled

The domain model implementations are relying on the persistence adapter entity classes and are not decoupled through 
the introduction of further abstraction with interfaces.

This coupling is a intentional decision made to simplify and shorten the implementations of the persistence layer.

