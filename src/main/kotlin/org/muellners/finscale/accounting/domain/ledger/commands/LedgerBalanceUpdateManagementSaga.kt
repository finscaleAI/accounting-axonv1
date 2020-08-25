package org.muellners.finscale.accounting.domain.ledger.commands

import java.math.BigDecimal
import java.util.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.ledger.api.AdjustLedgerTotalCommand
import org.muellners.finscale.accounting.domain.ledger.events.LedgerAccountCreatedEvent
import org.muellners.finscale.accounting.domain.ledger.events.LedgerAccountTransactionProcessedEvent
import org.muellners.finscale.accounting.domain.ledger.events.LedgerCreatedEvent
import org.muellners.finscale.accounting.domain.ledger.events.LedgerTotalAdjustedEvent
import org.springframework.beans.factory.annotation.Autowired

@Saga
class LedgerBalanceUpdateManagementSaga {

    @Autowired
    @Transient
    lateinit var commandGateway: CommandGateway

    val transactionId = UUID.randomUUID().toString()

    lateinit var transactionSide: TransactionSide

    lateinit var amount: BigDecimal

    val updatedLedgers: MutableSet<UUID> = mutableSetOf()

    var ledgerTreeUpdated = false

    @StartSaga
    @SagaEventHandler(associationProperty = "ledgerId")
    fun on(event: LedgerCreatedEvent) {
        this.transactionSide = TransactionSide.CREDIT
        this.amount = event.totalValue

        if (this.amount > BigDecimal.ZERO) {
            SagaLifecycle.associateWith("transactionId", transactionId)

            commandGateway.send<UUID>(
                AdjustLedgerTotalCommand(
                    transactionId = transactionId,
                    ledgerId = event.ledgerId,
                    transactionSide = this.transactionSide,
                    amount = this.amount
                ))
        } else {
            ledgerTreeUpdated = true
            SagaLifecycle.end()
        }
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "ledgerAccountId")
    fun on(event: LedgerAccountCreatedEvent) {
        this.transactionSide = TransactionSide.CREDIT
        this.amount = event.balance

        if (this.amount > BigDecimal.ZERO) {
            SagaLifecycle.associateWith("transactionId", this.transactionId)

            commandGateway.send<UUID>(
                AdjustLedgerTotalCommand(
                    transactionId = this.transactionId,
                    ledgerId = event.ledgerId,
                    transactionSide = this.transactionSide,
                    amount = this.amount
                ))
        } else {
            ledgerTreeUpdated = true
            SagaLifecycle.end()
        }
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "ledgerAccountId")
    fun on(event: LedgerAccountTransactionProcessedEvent) {
        this.transactionSide = event.transactionSide
        this.amount = event.amount

        if (this.amount > BigDecimal.ZERO) {
            SagaLifecycle.associateWith("transactionId", this.transactionId)

            commandGateway.send<UUID>(
                AdjustLedgerTotalCommand(
                    transactionId = this.transactionId,
                    ledgerId = event.ledgerId,
                    transactionSide = this.transactionSide,
                    amount = event.amount
                ))
        } else {
            ledgerTreeUpdated = true
            SagaLifecycle.end()
        }
    }

    @SagaEventHandler(associationProperty = "transactionId")
    fun on(event: LedgerTotalAdjustedEvent) {
        updatedLedgers.add(event.ledgerId)

        if (event.parentLedgerId == null) {
            ledgerTreeUpdated = true
            SagaLifecycle.end()
        }

        commandGateway.send<UUID>(
            AdjustLedgerTotalCommand(
                transactionId = this.transactionId,
                ledgerId = event.parentLedgerId,
                transactionSide = this.transactionSide,
                amount = this.amount
        ))
    }
}
