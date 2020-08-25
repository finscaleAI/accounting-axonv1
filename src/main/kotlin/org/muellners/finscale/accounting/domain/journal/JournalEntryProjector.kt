package org.muellners.finscale.accounting.domain.journal

import java.util.*
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.muellners.finscale.accounting.domain.enumeration.TransactionState
import org.muellners.finscale.accounting.domain.journal.events.JournalEntryPostedEvent
import org.muellners.finscale.accounting.domain.journal.events.JournalEntryReleasedEvent
import org.muellners.finscale.accounting.domain.journal.exceptions.JournalEntryNotFoundException
import org.muellners.finscale.accounting.domain.journal.queries.FindJournalEntryQuery
import org.muellners.finscale.accounting.domain.journal.views.JournalEntryView
import org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView
import org.muellners.finscale.accounting.domain.journal.views.TransactionView
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView
import org.muellners.finscale.accounting.repository.JournalEntryViewRepository
import org.springframework.stereotype.Component

@Component
class JournalEntryProjector(
    val journalEntryViewRepository: JournalEntryViewRepository
) {

    @EventHandler
    fun on(event: JournalEntryPostedEvent) {

        val transactions: MutableSet<TransactionView> = mutableSetOf()
        event.transactions.forEach {
            val transactionView = TransactionView(
                id = it.transactionId,
                transactionSide = it.transactionSide,
                amount = it.amount,
                account = LedgerAccountView(id = it.accountId),
                journalEntry = JournalEntryView(id = event.id)
            )

            transactions.add(transactionView)
        }

        val journalEntryView = JournalEntryView(
            id = event.id,
            transactionDate = event.transactionDate,
            transactionState = event.transactionState,
            note = event.note,
            message = event.message,
            transactions = transactions,
            transactionType = TransactionTypeView(id = event.transactionTypeId)
        )

        journalEntryViewRepository.save(journalEntryView)
    }

    @EventHandler
    fun on(event: JournalEntryReleasedEvent) {
        val journalEntryView = journalEntryViewRepository.findById(event.journalEntryId).get()

        journalEntryView.transactionState = TransactionState.PROCESSED
        journalEntryViewRepository.save(journalEntryView)
    }

    @QueryHandler
    fun handle(query: FindJournalEntryQuery): JournalEntryView {
        val optionalJournalEntryView = journalEntryViewRepository.findById(query.id)

        if (!optionalJournalEntryView.isPresent) {
            throw JournalEntryNotFoundException()
        }

        return optionalJournalEntryView.get()
    }
}
