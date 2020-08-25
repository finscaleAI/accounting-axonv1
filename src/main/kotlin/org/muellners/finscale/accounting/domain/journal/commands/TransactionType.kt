package org.muellners.finscale.accounting.domain.journal.commands

import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.muellners.finscale.accounting.domain.journal.api.CreateTransactionTypeCommand
import org.muellners.finscale.accounting.domain.journal.api.ModifyTransactionTypeCommand
import org.muellners.finscale.accounting.domain.journal.events.TransactionTypeCreatedEvent
import org.muellners.finscale.accounting.domain.journal.events.TransactionTypeModifiedEvent

@Aggregate
class TransactionType() {

    @AggregateIdentifier
    lateinit var typeId: UUID

    lateinit var identifier: String

    lateinit var name: String

    var description: String? = null

    @CommandHandler
    constructor(command: CreateTransactionTypeCommand) : this() {

        if (command.id == null) {
            throw IllegalArgumentException("Transaction Type ID cannot be null!")
        }

        if (command.identifier == null) {
            throw IllegalArgumentException("Transaction Type identifier cannot be null!")
        }

        if (command.name == null) {
            throw IllegalArgumentException("Transaction type name cannot be null!")
        }

        AggregateLifecycle.apply(
            TransactionTypeCreatedEvent(
                id = command.id,
                identifier = command.identifier,
                name = command.name,
                description = command.description
            )
        )
    }

    @CommandHandler
    fun handle(command: ModifyTransactionTypeCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Transaction Type ID cannot be null!")
        }

        val name = command.name ?: this.name
        val description = command.description ?: this.description

        AggregateLifecycle.apply(
            TransactionTypeModifiedEvent(
                id = command.id,
                name = name,
                description = description
            )
        )
    }

    @EventSourcingHandler
    fun on(event: TransactionTypeCreatedEvent) {
        this.typeId = event.id
        this.identifier = event.identifier
        this.name = event.name
        this.description = event.description
    }

    @EventSourcingHandler
    fun on(event: TransactionTypeModifiedEvent) {
        this.name = event.name
        this.description = event.description
    }
}
