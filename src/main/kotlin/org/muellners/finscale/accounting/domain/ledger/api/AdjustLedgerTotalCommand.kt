package org.muellners.finscale.accounting.domain.ledger.api

import java.math.BigDecimal
import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide

data class AdjustLedgerTotalCommand(
    val transactionId: String? = null,

    @TargetAggregateIdentifier
    val ledgerId: UUID? = null,

    val transactionSide: TransactionSide? = null,

    val amount: BigDecimal? = null

)
