package org.muellners.finscale.accounting.domain.ledger

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.muellners.finscale.accounting.domain.ledger.events.LedgerCreatedEvent
import org.muellners.finscale.accounting.domain.ledger.exceptions.LedgerNotFoundException
import org.muellners.finscale.accounting.domain.ledger.queries.FindAllLedgerQuery
import org.muellners.finscale.accounting.domain.ledger.queries.FindLedgerQuery
import org.muellners.finscale.accounting.domain.ledger.queries.LedgerIdentifierExistsQuery
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.muellners.finscale.accounting.repository.LedgerViewRepository
import org.springframework.stereotype.Component

@Component
class LedgerProjector(
    val ledgerViewRepository: LedgerViewRepository
) {

    @EventHandler
    fun on(event: LedgerCreatedEvent) {
        val parentLedger = if (event.parentLedgerId == null) null else LedgerView(id = event.parentLedgerId.toString())
        val ledgerView = LedgerView(
            id = event.id.toString(),
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

    @QueryHandler
    fun handle(query: FindLedgerQuery): LedgerView {
        val optionalLedgerView = ledgerViewRepository.findById(query.id)

        if (!optionalLedgerView.isPresent) {
            throw LedgerNotFoundException()
        }

        return optionalLedgerView.get()
    }

    @QueryHandler
    fun handle(query: LedgerIdentifierExistsQuery): Boolean = ledgerViewRepository
        .findByIdentifier(query.identifier).isPresent

    @QueryHandler
    fun handle(query: FindAllLedgerQuery) = ledgerViewRepository.findAll()
}
