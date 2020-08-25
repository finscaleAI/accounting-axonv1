package org.muellners.finscale.accounting.domain.ledger.commands

import java.math.BigDecimal
import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.muellners.finscale.accounting.domain.enumeration.LedgerType
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.ledger.api.*
import org.muellners.finscale.accounting.domain.ledger.events.*

@Aggregate
class Ledger() {

    @AggregateIdentifier
    lateinit var ledgerId: UUID

    lateinit var identifier: String

    lateinit var name: String

    lateinit var type: LedgerType

    var description: String? = null

    lateinit var totalValue: BigDecimal

    var showAccountsInChart: Boolean = true

    var parentLedgerId: UUID? = null

    @CommandHandler
    constructor(command: CreateLedgerCommand) : this() {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        if (command.identifier == null) {
            throw IllegalArgumentException("Ledger identifier cannot be null!")
        }

        if (command.name == null) {
            throw IllegalArgumentException("Ledger name cannot be null!")
        }

        if (command.type == null) {
            throw IllegalArgumentException("Ledger type cannot be null!")
        }

        val totalValue = command.totalValue ?: BigDecimal.ZERO
        val showAccountsInChart = command.showAccountsInChart ?: true

        AggregateLifecycle.apply(
            LedgerCreatedEvent(
                ledgerId = command.id,
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
    fun handle(command: ModifyLedgerCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        val name = command.name ?: this.name
        val description = command.description ?: this.description
        val showAccountsInChart = command.showAccountsInChart ?: this.showAccountsInChart

        AggregateLifecycle.apply(
            LedgerModifiedEvent(
                id = command.id,
                name = name,
                description = description,
                showAccountsInChart = showAccountsInChart
            )
        )
    }

    @CommandHandler
    fun handle(command: AdjustLedgerTotalCommand) {

        if (command.transactionId == null) {
            throw IllegalArgumentException("Transaction ID cannot be null!")
        }

        if (command.ledgerId == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        if (command.transactionSide == null) {
            throw IllegalArgumentException("Transaction side cannot be null!")
        }

        if (command.amount == null) {
            throw IllegalArgumentException("Amount to be adjusted cannot be null!")
        }

        if (command.amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Amount to be adjusted should be a positive number!")
        }

        val currentTotalValue = when (command.transactionSide) {
            TransactionSide.CREDIT -> this.totalValue + command.amount
            TransactionSide.DEBIT -> this.totalValue - command.amount
        }

        AggregateLifecycle.apply(
            LedgerTotalAdjustedEvent(
                transactionId = command.transactionId,
                ledgerId = command.ledgerId,
                parentLedgerId = this.parentLedgerId,
                currentTotalValue = currentTotalValue
            )
        )
    }

    @CommandHandler
    fun handle(command: DeleteLedgerCommand) = AggregateLifecycle.apply(
        LedgerDeletedEvent(id = command.id)
    )

    @EventSourcingHandler
    fun on(event: LedgerCreatedEvent) {
        this.ledgerId = event.ledgerId
        this.identifier = event.identifier
        this.name = event.name
        this.type = event.type
        this.description = event.description
        this.totalValue = BigDecimal.ZERO
        this.showAccountsInChart = event.showAccountsInChart
        this.parentLedgerId = event.parentLedgerId
    }

    @EventSourcingHandler
    fun on(event: LedgerModifiedEvent) {
        this.name = event.name
        this.description = event.description
        this.showAccountsInChart = event.showAccountsInChart
    }

    @EventSourcingHandler
    fun on(event: LedgerDeletedEvent) {
        AggregateLifecycle.markDeleted()
    }

    @EventSourcingHandler
    fun on(event: LedgerTotalAdjustedEvent) {
        this.totalValue = event.currentTotalValue
    }
}
