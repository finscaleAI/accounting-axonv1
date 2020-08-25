package org.muellners.finscale.accounting.domain.ledger.commands

import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.muellners.finscale.accounting.domain.enumeration.AccountAction
import org.muellners.finscale.accounting.domain.enumeration.AccountState
import org.muellners.finscale.accounting.domain.enumeration.LedgerType
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.ledger.api.*
import org.muellners.finscale.accounting.domain.ledger.events.*

@Aggregate
class LedgerAccount() {

    @AggregateIdentifier
    lateinit var accountId: UUID

    lateinit var ledgerId: UUID

    lateinit var identifier: String

    var alternativeAccountNumber: String? = null

    lateinit var name: String

    lateinit var type: LedgerType

    lateinit var state: AccountState

    lateinit var balance: BigDecimal

    lateinit var holderIds: MutableList<String>

    lateinit var signatureAuthorityIds: MutableList<String>

    var performedActions: MutableSet<LedgerAccountAction> = mutableSetOf()

    var entries: MutableSet<LedgerAccountEntry> = mutableSetOf()

    var referenceAccountId: UUID? = null

    @CommandHandler
    constructor(command: CreateLedgerAccountCommand) : this() {

        if (command.id == null) {
            throw IllegalArgumentException("Account ID cannot be null!")
        }

        if (command.ledgerId == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        if (command.identifier == null) {
            throw IllegalArgumentException("Account identifier cannot be null!")
        }

        if (command.name == null) {
            throw IllegalArgumentException("Account name cannot be null!")
        }

        val balance = command.balance ?: BigDecimal.ZERO

        AggregateLifecycle.apply(
            LedgerAccountCreatedEvent(
                ledgerAccountId = command.id.toString(),
                ledgerId = command.ledgerId,
                identifier = command.identifier,
                alternativeAccountNumber = command.alternativeAccountNumber,
                name = command.name,
                type = command.type,
                state = AccountState.OPEN,
                balance = balance,
                holderIds = command.holderIds!!.toMutableList(),
                signatureAuthorityIds = command.signatureAuthorityIds!!.toMutableList(),
                referenceAccountId = command.referenceAccountId
            )
        )
    }

    @CommandHandler
    fun handle(command: LockLedgerAccountCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        val action = AccountAction.LOCK

        if (this.state == AccountState.OPEN) {
            AggregateLifecycle.apply(
                LedgerAccountActionPerformedEvent(
                    accountId = command.id,
                    actionId = UUID.randomUUID(),
                    action = action,
                    accountState = getNextState(action),
                    comment = command.comment,
                    occurredAt = ZonedDateTime.now()
                )
            )
        } else {
            throw Exception("Only open accounts could be locked!")
        }
    }

    @CommandHandler
    fun handle(command: UnlockLedgerAccountCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        val action = AccountAction.UNLOCK

        if (this.state == AccountState.LOCKED) {
            AggregateLifecycle.apply(
                LedgerAccountActionPerformedEvent(
                    accountId = command.id,
                    actionId = UUID.randomUUID(),
                    action = action,
                    accountState = getNextState(action),
                    comment = command.comment,
                    occurredAt = ZonedDateTime.now()
                )
            ) } else {
            throw Exception("Only locked accounts could be unlocked!")
        }
    }

    @CommandHandler
    fun handle(command: CloseLedgerAccountCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        val action = AccountAction.CLOSE

        if (this.state == AccountState.OPEN) {
            AggregateLifecycle.apply(
                LedgerAccountActionPerformedEvent(
                    accountId = command.id,
                    actionId = UUID.randomUUID(),
                    action = action,
                    accountState = getNextState(action),
                    comment = command.comment,
                    occurredAt = ZonedDateTime.now()
                )
            )
        } else {
            throw Exception("Only open accounts could be closed!")
        }
    }

    @CommandHandler
    fun handle(command: ReopenLedgerAccountCommand) {

        if (command.id == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        val action = AccountAction.REOPEN

        if (this.state == AccountState.LOCKED) {
            AggregateLifecycle.apply(
                LedgerAccountActionPerformedEvent(
                    accountId = command.id,
                    actionId = UUID.randomUUID(),
                    action = action,
                    accountState = getNextState(action),
                    comment = command.comment,
                    occurredAt = ZonedDateTime.now()
                )
            )
        } else {
            throw Exception("Only closed accounts could be reopened!")
        }
    }

    @CommandHandler
    fun handle(command: DeleteLedgerAccountCommand) {

        if (command.accountId == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        AggregateLifecycle.apply(LedgerAccountDeletedEvent(id = command.accountId))
    }

    @CommandHandler
    fun handle(command: ProcessLedgerAccountTransactionCommand) {

        if (command.ledgerAccountId == null) {
            throw IllegalArgumentException("Ledger account ID cannot be null!")
        }

        if (command.entryId == null) {
            throw IllegalArgumentException("Entry ID cannot be null!")
        }

        if (command.transactionId == null) {
            throw IllegalArgumentException("Transaction ID cannot be null!")
        }

        if (command.transactionSide == null) {
            throw IllegalArgumentException("Transaction side cannot be null!")
        }

        if (command.amount == null) {
            throw IllegalArgumentException("Transaction amount cannot be null!")
        }

        val actualTransactionSide =
            when (this.type) {
                LedgerType.ASSET,
                LedgerType.EXPENSE -> command.transactionSide.inverse()

                LedgerType.LIABILITY,
                LedgerType.EQUITY,
                LedgerType.REVENUE -> command.transactionSide
        }

        val balanceAfterTransaction =
            when (actualTransactionSide) {
                TransactionSide.DEBIT -> this.balance - command.amount
                TransactionSide.CREDIT -> this.balance + command.amount
            }

        AggregateLifecycle.apply(
            LedgerAccountTransactionProcessedEvent(
                ledgerAccountId = command.ledgerAccountId,
                ledgerId = this.ledgerId,
                entryId = command.entryId.toString(),
                transactionId = command.transactionId,
                transactionSide = actualTransactionSide,
                amount = command.amount,
                balance = balanceAfterTransaction,
                processedOn = LocalDate.now()
            )
        )
    }

    @EventSourcingHandler
    fun on(event: LedgerAccountCreatedEvent) {
        this.accountId = UUID.fromString(event.ledgerAccountId)
        this.ledgerId = event.ledgerId
        this.identifier = event.identifier
        this.alternativeAccountNumber = event.alternativeAccountNumber
        this.name = event.name
        this.type = event.type
        this.state = event.state
        this.balance = event.balance
        this.holderIds = event.holderIds.toMutableList()
        this.signatureAuthorityIds = event.signatureAuthorityIds.toMutableList()
        this.referenceAccountId = event.referenceAccountId
    }

    @EventSourcingHandler
    fun on(event: LedgerAccountActionPerformedEvent) {
        this.state = event.accountState

        val action = LedgerAccountAction()
        action.actionId = event.actionId
        action.comment = event.comment
        action.occurredAt = event.occurredAt

        performedActions.add(action)
    }

    @EventSourcingHandler
    fun on(event: LedgerAccountTransactionProcessedEvent) {
        this.balance = event.balance

        val entry = LedgerAccountEntry()
        entry.entryId = UUID.fromString(event.entryId)
        entry.transactionId = event.transactionId
        entry.transactionSide = event.transactionSide
        entry.amount = event.amount
        entry.balance = event.balance
        entry.processedOn = event.processedOn

        this.entries.add(entry)
    }

    @EventSourcingHandler
    fun on(event: LedgerAccountDeletedEvent) =
        AggregateLifecycle.markDeleted()

    private fun getNextState(action: AccountAction) =
        when (action) {
            AccountAction.LOCK -> AccountState.LOCKED
            AccountAction.CLOSE -> AccountState.CLOSED
            AccountAction.UNLOCK -> AccountState.OPEN
            AccountAction.REOPEN -> AccountState.OPEN
        }
}
