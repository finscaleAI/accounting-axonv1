package org.muellners.finscale.accounting.domain.ledger.events

import java.math.BigDecimal
import java.util.*
import org.muellners.finscale.accounting.domain.enumeration.LedgerType

data class LedgerCreatedEvent(
    var ledgerId: UUID,

    var identifier: String,

    var name: String,

    var type: LedgerType,

    var description: String? = null,

    var totalValue: BigDecimal = BigDecimal.ZERO,

    var showAccountsInChart: Boolean = true,

    var parentLedgerId: UUID? = null

)
