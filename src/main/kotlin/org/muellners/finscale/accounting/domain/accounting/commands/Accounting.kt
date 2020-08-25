package org.muellners.finscale.accounting.domain.accounting.commands

import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.muellners.finscale.accounting.domain.enumeration.AccountState
import org.muellners.finscale.accounting.domain.ledger.api.*
import org.muellners.finscale.accounting.domain.ledger.queries.FindLedgerAccountQuery
import org.muellners.finscale.accounting.domain.ledger.queries.FindLedgerQuery
import org.muellners.finscale.accounting.domain.ledger.queries.LedgerAccountIdentifierExistsQuery
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.springframework.stereotype.Component

@Component
class Accounting(
    val commandGateway: CommandGateway,
    val queryGateway: QueryGateway
) {

    @CommandHandler
    fun handle(command: ValidateAndCreateLedgerAccountCommand): UUID? {

        if (command.identifier == null) {
            throw IllegalArgumentException("Account identifier cannot be null!")
        }

        val identifierExists = queryGateway.query(
            LedgerAccountIdentifierExistsQuery(identifier = command.identifier),
            ResponseTypes.instanceOf(Boolean::class.java)).get()

        if (identifierExists) {
            throw IllegalArgumentException("A ledger account with the same identifier exists")
        }

        if (command.ledgerId == null) {
            throw IllegalArgumentException("Ledger ID cannot be null!")
        }

        val ledger = queryGateway.query(
            FindLedgerQuery(id = command.ledgerId),
            ResponseTypes.instanceOf(LedgerView::class.java)).get()
        val ledgerType = ledger.type ?: throw IllegalArgumentException("Ledger type cannot be null!")

        if (command.referenceAccountId != null) {
            val referenceAccount = queryGateway.query(
                FindLedgerAccountQuery(id = command.referenceAccountId),
                ResponseTypes.instanceOf(LedgerAccountView::class.java)).get()

            if (referenceAccount.state != AccountState.OPEN) {
                throw IllegalArgumentException("Reference account should be open!")
            }
        }

        commandGateway.send<UUID>(
            CreateLedgerAccountCommand(
                id = command.id,
                ledgerId = command.ledgerId,
                type = ledgerType,
                identifier = command.identifier,
                alternativeAccountNumber = command.alternativeAccountNumber,
                name = command.name,
                balance = command.balance,
                holderIds = command.holderIds,
                signatureAuthorityIds = command.signatureAuthorityIds,
                referenceAccountId = command.referenceAccountId
            )
        )

        return command.id
    }
}
