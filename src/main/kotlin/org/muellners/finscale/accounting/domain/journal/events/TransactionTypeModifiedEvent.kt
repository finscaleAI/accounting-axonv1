package org.muellners.finscale.accounting.domain.journal.events

import java.util.*

data class TransactionTypeModifiedEvent(
    val id: UUID,

    val name: String,

    val description: String?

)
