package org.muellners.finscale.accounting.domain.journal.events

import java.util.*

data class TransactionTypeCreatedEvent(
    val id: UUID,

    val identifier: String,

    val name: String,

    val description: String?

)
