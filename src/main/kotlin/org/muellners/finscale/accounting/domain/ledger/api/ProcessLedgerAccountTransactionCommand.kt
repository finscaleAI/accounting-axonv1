package org.muellners.finscale.accounting.domain.ledger.api

import java.math.BigDecimal
import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide

data class ProcessLedgerAccountTransactionCommand(
    val transactionId: UUID? = null,

    @TargetAggregateIdentifier
    val ledgerAccountId: UUID? = null,

    val entryId: UUID? = null,

    val transactionSide: TransactionSide? = null,

    val amount: BigDecimal? = null

)
