package org.muellners.finscale.accounting.domain.journal

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.muellners.finscale.accounting.domain.journal.events.TransactionTypeCreatedEvent
import org.muellners.finscale.accounting.domain.journal.events.TransactionTypeModifiedEvent
import org.muellners.finscale.accounting.domain.journal.exceptions.TransactionTypeNotFoundException
import org.muellners.finscale.accounting.domain.journal.queries.FindAllTransactionTypesQuery
import org.muellners.finscale.accounting.domain.journal.queries.FindTransactionTypeQuery
import org.muellners.finscale.accounting.domain.journal.queries.TransactionTypeIdentifierExistsQuery
import org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView
import org.muellners.finscale.accounting.repository.TransactionTypeViewRepository
import org.springframework.stereotype.Component

@Component
class TransactionTypeProjector(
    val transactionTypeViewRepository: TransactionTypeViewRepository
) {

    @EventHandler
    fun on(event: TransactionTypeCreatedEvent) {
        val transactionTypeView = TransactionTypeView(
            id = event.id,
            identifier = event.identifier,
            name = event.name,
            description = event.description
        )

        transactionTypeViewRepository.save(transactionTypeView)
    }

    @EventHandler
    fun on(event: TransactionTypeModifiedEvent) {
        val transactionTypeView = transactionTypeViewRepository.findById(event.id).get()

        transactionTypeView.name = event.name
        transactionTypeView.description = event.description
        transactionTypeViewRepository.save(transactionTypeView)
    }

    @QueryHandler
    fun handle(query: TransactionTypeIdentifierExistsQuery): Boolean = transactionTypeViewRepository
        .findByIdentifier(query.identifier).isPresent

    @QueryHandler
    fun handle(query: FindTransactionTypeQuery): TransactionTypeView {
        val optionalTransactionTypeView = transactionTypeViewRepository.findById(query.id)

        if (!optionalTransactionTypeView.isPresent) {
            throw TransactionTypeNotFoundException()
        }

        return optionalTransactionTypeView.get()
    }

    @QueryHandler
    fun handle(query: FindAllTransactionTypesQuery) = transactionTypeViewRepository.findAll()
}
