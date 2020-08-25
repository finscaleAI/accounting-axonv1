package org.muellners.finscale.accounting.domain.ledger.events

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide

data class LedgerAccountTransactionProcessedEvent(
    val ledgerAccountId: UUID,

    val ledgerId: UUID,

    val entryId: String,

    val transactionId: UUID,

    val transactionSide: TransactionSide,

    val amount: BigDecimal,

    val balance: BigDecimal,

    val processedOn: LocalDate

)
