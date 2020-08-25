package org.muellners.finscale.accounting.domain.ledger

import java.util.*
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.muellners.finscale.accounting.domain.journal.views.TransactionView
import org.muellners.finscale.accounting.domain.ledger.events.*
import org.muellners.finscale.accounting.domain.ledger.exceptions.LedgerAccountNotFoundException
import org.muellners.finscale.accounting.domain.ledger.queries.*
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountActionView
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountEntryView
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.muellners.finscale.accounting.repository.LedgerAccountViewRepository
import org.springframework.stereotype.Component

@Component
class LedgerAccountProjector(
    val ledgerAccountViewRepository: LedgerAccountViewRepository
) {

    @EventHandler
    fun on(event: LedgerAccountCreatedEvent) {
        val referenceAccount = if (event.referenceAccountId != null)
            LedgerAccountView(id = event.referenceAccountId) else null

        val ledgerAccountView = LedgerAccountView(
            id = UUID.fromString(event.ledgerAccountId),
            identifier = event.identifier,
            alternativeAccountNumber = event.alternativeAccountNumber,
            name = event.name,
            type = event.type,
            state = event.state,
            balance = event.balance,
            holders = event.holderIds.joinToString(","),
            signatureAuthorities = event.signatureAuthorityIds.joinToString(","),
            ledger = LedgerView(id = event.ledgerId),
            referenceAccount = referenceAccount
        )

        ledgerAccountViewRepository.save(ledgerAccountView)
    }

    @EventHandler
    fun on(event: LedgerAccountActionPerformedEvent) {
        val ledgerAccountView = ledgerAccountViewRepository.findById(event.accountId).get()
        val ledgerAccountAction = LedgerAccountActionView(
            id = event.actionId,
            ledgerAccount = ledgerAccountView,
            action = event.action,
            comment = event.comment,
            occuredAt = event.occurredAt
        )

        ledgerAccountView.state = event.accountState
        ledgerAccountView.actions.add(ledgerAccountAction)
        ledgerAccountViewRepository.save(ledgerAccountView)
    }

    @EventHandler
    fun on(event: LedgerAccountTransactionProcessedEvent) {
        val ledgerAccountView = ledgerAccountViewRepository.findById(event.ledgerAccountId).get()
        val ledgerAccountEntryView = LedgerAccountEntryView(
            id = UUID.fromString(event.entryId),
            side = event.transactionSide,
            amount = event.amount,
            balance = event.balance,
            processedOn = event.processedOn,
            transaction = TransactionView(id = event.transactionId),
            ledgerAccount = ledgerAccountView
        )

        ledgerAccountView.balance = event.balance
        ledgerAccountView.entries.add(ledgerAccountEntryView)
        ledgerAccountViewRepository.save(ledgerAccountView)
    }

    @EventHandler
    fun on(event: LedgerAccountDeletedEvent) {
        ledgerAccountViewRepository.deleteById(event.id)
    }

    @QueryHandler
    fun handle(query: FindLedgerAccountQuery): LedgerAccountView {
        val optionalLedgerAccountView = ledgerAccountViewRepository.findById(query.id)

        if (!optionalLedgerAccountView.isPresent) {
            throw LedgerAccountNotFoundException()
        }

        return optionalLedgerAccountView.get()
    }

    @QueryHandler
    fun handle(query: LedgerAccountIdentifierExistsQuery): Boolean = ledgerAccountViewRepository
        .findByIdentifier(query.identifier).isPresent

    @QueryHandler
    fun handle(query: FindAllLedgerAccountsQuery) = ledgerAccountViewRepository.findAll()

    @QueryHandler
    fun handle(query: FindAllLedgerAccountActionsQuery): MutableSet<LedgerAccountActionView> {
        val optionalLedgerAccountView = ledgerAccountViewRepository.findById(query.ledgerId)

        return if (optionalLedgerAccountView.isPresent)
            optionalLedgerAccountView.get().actions else
            throw LedgerAccountNotFoundException()
    }

    @QueryHandler
    fun handle(query: FindAllLedgerAccountEntriesQuery): MutableSet<LedgerAccountEntryView> {
        val optionalLedgerAccountView = ledgerAccountViewRepository.findById(query.ledgerId)

        return if (optionalLedgerAccountView.isPresent)
            optionalLedgerAccountView.get().entries else
            throw LedgerAccountNotFoundException()
    }
}
