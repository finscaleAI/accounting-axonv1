package org.muellners.finscale.accounting.domain.ledger

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.muellners.finscale.accounting.domain.ledger.events.LedgerCreatedEvent
import org.muellners.finscale.accounting.domain.ledger.events.LedgerDeletedEvent
import org.muellners.finscale.accounting.domain.ledger.events.LedgerModifiedEvent
import org.muellners.finscale.accounting.domain.ledger.events.LedgerTotalAdjustedEvent
import org.muellners.finscale.accounting.domain.ledger.exceptions.LedgerNotFoundException
import org.muellners.finscale.accounting.domain.ledger.queries.FindAllLedgersQuery
import org.muellners.finscale.accounting.domain.ledger.queries.FindLedgerQuery
import org.muellners.finscale.accounting.domain.ledger.queries.LedgerIdentifierExistsQuery
import org.muellners.finscale.accounting.domain.ledger.queries.SubLedgersExistQuery
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.muellners.finscale.accounting.repository.LedgerViewRepository
import org.springframework.stereotype.Component

@Component
class LedgerProjector(
    val ledgerViewRepository: LedgerViewRepository
) {

    @EventHandler
    fun on(event: LedgerCreatedEvent) {
        val parentLedger = if (event.parentLedgerId != null)
            LedgerView(id = event.parentLedgerId) else null

        val ledgerView = LedgerView(
            id = event.ledgerId,
            identifier = event.identifier,
            name = event.name,
            type = event.type,
            description = event.description,
            totalValue = event.totalValue,
            showAccountsInChart = event.showAccountsInChart,
            parentLedger = parentLedger
        )

        ledgerViewRepository.save(ledgerView)
    }

    @EventHandler
    fun on(event: LedgerModifiedEvent) {
        val optionalLedgerView = ledgerViewRepository.findById(event.id)
        val ledgerView = if (optionalLedgerView.isPresent)
            optionalLedgerView.get() else
            throw LedgerNotFoundException()

        ledgerView.name = event.name
        ledgerView.description = event.description
        ledgerView.showAccountsInChart = event.showAccountsInChart

        ledgerViewRepository.save(ledgerView)
    }

    @EventHandler
    fun on(event: LedgerDeletedEvent) = ledgerViewRepository.deleteById(event.id)

    @EventHandler
    fun on(event: LedgerTotalAdjustedEvent) {
        val optionalLedgerView = ledgerViewRepository.findById(event.ledgerId)

        if (!optionalLedgerView.isPresent) {
            throw LedgerNotFoundException()
        }

        val ledgerView = optionalLedgerView.get()
        ledgerView.totalValue = event.currentTotalValue
        ledgerViewRepository.save(ledgerView)
    }

    @QueryHandler
    fun handle(query: FindLedgerQuery): LedgerView {
        val optionalLedgerView = ledgerViewRepository.findById(query.id)

        if (!optionalLedgerView.isPresent) {
            throw LedgerNotFoundException()
        }

        return optionalLedgerView.get()
    }

    @QueryHandler
    fun handle(query: FindAllLedgersQuery) = ledgerViewRepository.findAll()

    @QueryHandler
    fun handle(query: LedgerIdentifierExistsQuery): Boolean = ledgerViewRepository
        .findByIdentifier(query.identifier).isPresent

    @QueryHandler
    fun handle(query: SubLedgersExistQuery): Boolean {
        val optionalParentLedgerView = ledgerViewRepository.findById(query.parentLedgerId)
        val parentLedgerView = if (optionalParentLedgerView.isPresent)
            optionalParentLedgerView.get() else
            throw LedgerNotFoundException()

        return parentLedgerView.subledgers.size > 0
    }
}
