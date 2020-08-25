package org.muellners.finscale.accounting.domain.ledger.queries

import java.util.*

data class FindAllLedgerAccountActionsQuery(
    val ledgerId: UUID

)
