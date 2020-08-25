package org.muellners.finscale.accounting.domain.ledger.queries

import java.util.*

data class SubLedgersExistQuery(
    val parentLedgerId: UUID

)
