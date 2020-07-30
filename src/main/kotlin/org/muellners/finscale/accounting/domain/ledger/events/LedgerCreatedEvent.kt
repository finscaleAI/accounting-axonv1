package org.muellners.finscale.accounting.domain.ledger.events

import java.math.BigDecimal
import java.util.*
import org.muellners.finscale.accounting.domain.AccountType

data class LedgerCreatedEvent(
    var id: UUID,

    var identifier: String,

    var name: String,

    var type: AccountType,

    var description: String? = null,

    var totalValue: BigDecimal = BigDecimal.ZERO,

    var showAccountsInChart: Boolean = true,

    var parentLedgerId: UUID? = null

)
