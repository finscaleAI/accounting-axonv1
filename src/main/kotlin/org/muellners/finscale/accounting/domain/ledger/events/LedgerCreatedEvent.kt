package org.muellners.finscale.accounting.domain.ledger.events

import java.math.BigDecimal
import java.util.*

data class LedgerCreatedEvent(
    var id: UUID,

    var identifier: String,

    var name: String,

    var type: String,

    var description: String? = null,

    var totalValue: BigDecimal = BigDecimal.ZERO,

    var showAccountsInChart: Boolean = true,

    var parentLedgerId: UUID? = null

)
