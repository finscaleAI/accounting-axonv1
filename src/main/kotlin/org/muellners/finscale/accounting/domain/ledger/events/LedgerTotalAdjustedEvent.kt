package org.muellners.finscale.accounting.domain.ledger.events

import java.math.BigDecimal
import java.util.*

data class LedgerTotalAdjustedEvent(
    val transactionId: String,

    val ledgerId: UUID,

    val parentLedgerId: UUID?,

    val currentTotalValue: BigDecimal

)
