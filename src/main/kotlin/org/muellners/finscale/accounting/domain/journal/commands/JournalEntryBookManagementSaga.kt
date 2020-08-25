package org.muellners.finscale.accounting.domain.journal.commands

import java.util.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.muellners.finscale.accounting.domain.journal.api.ReleaseJournalEntryCommand
import org.muellners.finscale.accounting.domain.journal.events.JournalEntryBookedEvent
import org.muellners.finscale.accounting.domain.journal.events.JournalEntryReleasedEvent
import org.muellners.finscale.accounting.domain.ledger.api.ProcessLedgerAccountTransactionCommand
import org.muellners.finscale.accounting.domain.ledger.events.LedgerAccountTransactionProcessedEvent
import org.springframework.beans.factory.annotation.Autowired

@Saga
class JournalEntryBookManagementSaga {

    @Autowired
    @Transient
    lateinit var commandGateway: CommandGateway

    lateinit var journalEntryId: UUID

    lateinit var transactions: Set<Transaction>

    val processedTransactionIds: MutableSet<UUID> = mutableSetOf()

    var journalEntryBooked = false

    @StartSaga
    @SagaEventHandler(associationProperty = "journalEntryId")
    fun on(event: JournalEntryBookedEvent) {
        this.journalEntryId = event.journalEntryId
        this.transactions = event.transactions

        this.transactions.forEach {
            val entryId = UUID.randomUUID()
            SagaLifecycle.associateWith("entryId", entryId.toString())

            this.commandGateway.send<UUID>(
                ProcessLedgerAccountTransactionCommand(
                    transactionId = it.transactionId,
                    ledgerAccountId = it.accountId,
                    entryId = entryId,
                    transactionSide = it.transactionSide,
                    amount = it.amount
                )
            )
        }
    }

    @SagaEventHandler(associationProperty = "entryId")
    fun handle(event: LedgerAccountTransactionProcessedEvent) {
        this.processedTransactionIds.add(event.transactionId)
        val allTransactionsBooked = this.processedTransactionIds.containsAll(
            this.transactions.map { it.transactionId }
        )

        if (allTransactionsBooked) {
            SagaLifecycle.associateWith("journalEntryId", this.journalEntryId.toString())
            this.commandGateway.send<UUID>(
                ReleaseJournalEntryCommand(id = this.journalEntryId)
            )
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "journalEntryId")
    fun handle(event: JournalEntryReleasedEvent) {
        this.journalEntryBooked = true
    }
}
