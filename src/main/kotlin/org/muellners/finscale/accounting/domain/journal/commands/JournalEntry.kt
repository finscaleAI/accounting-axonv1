package org.muellners.finscale.accounting.domain.journal.commands

import java.time.LocalDate
import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.spring.stereotype.Aggregate
import org.muellners.finscale.accounting.domain.accounting.api.BookJournalEntryCommand
import org.muellners.finscale.accounting.domain.accounting.exceptions.JournalEntryAlreadyProcessedException
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.enumeration.TransactionState
import org.muellners.finscale.accounting.domain.journal.api.PostJournalEntryCommand
import org.muellners.finscale.accounting.domain.journal.api.ReleaseJournalEntryCommand
import org.muellners.finscale.accounting.domain.journal.events.JournalEntryBookedEvent
import org.muellners.finscale.accounting.domain.journal.events.JournalEntryPostedEvent
import org.muellners.finscale.accounting.domain.journal.events.JournalEntryReleasedEvent

@Aggregate
class JournalEntry() {

    @AggregateIdentifier
    lateinit var entryId: UUID

    lateinit var transactionDate: LocalDate

    lateinit var transactionState: TransactionState

    lateinit var transactionTypeId: UUID

    @AggregateMember
    var transactions: MutableSet<Transaction> = mutableSetOf()

    var note: String? = null

    var message: String? = null

    @CommandHandler
    constructor(command: PostJournalEntryCommand) : this() {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        if (command.transactionTypeId == null) {
            throw IllegalArgumentException("Transaction type ID cannot be null!")
        }

        val transactionDate = command.transactionDate ?: LocalDate.now()
        val transactions: MutableSet<Transaction> = mutableSetOf()

        command.debtors.forEach {
            val transaction = Transaction()
            transaction.transactionId = UUID.randomUUID()
            transaction.accountId = it.key
            transaction.transactionSide = TransactionSide.DEBIT
            transaction.amount = it.value

            transactions.add(transaction)
        }

        command.creditors.forEach {
            val transaction = Transaction()
            transaction.transactionId = UUID.randomUUID()
            transaction.accountId = it.key
            transaction.transactionSide = TransactionSide.CREDIT
            transaction.amount = it.value

            transactions.add(transaction)
        }

        AggregateLifecycle.apply(
            JournalEntryPostedEvent(
                id = command.id,
                transactionDate = transactionDate,
                transactionTypeId = command.transactionTypeId,
                transactionState = TransactionState.PENDING,
                transactions = transactions,
                note = command.note,
                message = command.message
            )
        )
    }

    @CommandHandler
    fun handle(command: BookJournalEntryCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        if (this.transactionState == TransactionState.PROCESSED) {
            throw JournalEntryAlreadyProcessedException()
        }

        AggregateLifecycle.apply(
            JournalEntryBookedEvent(
                journalEntryId = command.id,
                transactions = this.transactions
            )
        )
    }

    @CommandHandler
    fun handle(command: ReleaseJournalEntryCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        AggregateLifecycle.apply(JournalEntryReleasedEvent(journalEntryId = command.id))
    }

    @EventSourcingHandler
    fun on(event: JournalEntryPostedEvent) {
        this.entryId = event.id
        this.transactionDate = event.transactionDate
        this.transactionState = event.transactionState
        this.transactionTypeId = event.transactionTypeId
        this.transactions = event.transactions
        this.note = event.note
        this.message = event.message
    }

    @EventSourcingHandler
    fun on(event: JournalEntryReleasedEvent) {
        this.transactionState = TransactionState.PROCESSED
    }
}
