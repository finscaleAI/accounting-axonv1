package org.muellners.finscale.accounting.domain.ledger.events

import java.util.*

data class LedgerModifiedEvent(
    val id: UUID,

    val name: String,

    val description: String?,

    val showAccountsInChart: Boolean
)
