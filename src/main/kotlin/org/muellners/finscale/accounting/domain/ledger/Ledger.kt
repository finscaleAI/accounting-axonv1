package org.muellners.finscale.accounting.domain.ledger

import java.math.BigDecimal
import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.muellners.finscale.accounting.domain.ledger.commands.CreateLedgerCommand
import org.muellners.finscale.accounting.domain.ledger.commands.DeleteLedgerCommand
import org.muellners.finscale.accounting.domain.ledger.events.LedgerCreatedEvent
import org.muellners.finscale.accounting.domain.ledger.events.LedgerDeletedEvent

@Aggregate
class Ledger() {

//    @Autowired
//    lateinit var queryGateway: QueryGateway

    @AggregateIdentifier
    lateinit var ledgerId: UUID

    lateinit var identifier: String

    lateinit var name: String

    lateinit var type: String

    var description: String? = null

    lateinit var totalValue: BigDecimal

    var showAccountsInChart: Boolean = true

    var parentLedgerId: UUID? = null

    @CommandHandler
    constructor(command: CreateLedgerCommand) : this() {

        val totalValue = command.totalValue ?: BigDecimal.ZERO
        val showAccountsInChart = command.showAccountsInChart ?: true

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        if (command.identifier == null) {
            throw IllegalArgumentException("Ledger identifier cannot be null!")
        } // else {
//            val queryMessage = LedgerIdentifierExistsQuery(command.identifier)
//            val identifierExistsFuture = queryGateway.query(queryMessage, ResponseTypes.instanceOf(Boolean::class.java))
//
//            if (identifierExistsFuture.get()) {
//                throw LedgerIdentifierExistsException()
//            }
//        }

        if (command.name == null) {
            throw IllegalArgumentException("Ledger name cannot be null!")
        }

        if (command.type == null) {
            throw IllegalArgumentException("Ledger type cannot be null!")
        }

        AggregateLifecycle.apply(
            LedgerCreatedEvent(
                id = command.id,
                identifier = command.identifier,
                name = command.name,
                type = command.type,
                description = command.description,
                totalValue = totalValue,
                showAccountsInChart = showAccountsInChart,
                parentLedgerId = command.parentLedgerId
            )
        )
    }

    @CommandHandler
    fun handle(command: DeleteLedgerCommand) {
        AggregateLifecycle.apply(LedgerDeletedEvent(command.id.toString()))
        AggregateLifecycle.markDeleted()
    }

    @EventSourcingHandler
    fun on(event: LedgerCreatedEvent) {
        this.ledgerId = event.id
        this.identifier = event.identifier
        this.name = event.name
        this.type = event.type
        this.description = event.description
        this.totalValue = event.totalValue
        this.showAccountsInChart = event.showAccountsInChart
        this.parentLedgerId = event.parentLedgerId
    }
}
