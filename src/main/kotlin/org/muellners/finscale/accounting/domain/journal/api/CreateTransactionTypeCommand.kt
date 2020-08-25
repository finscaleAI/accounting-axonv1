package org.muellners.finscale.accounting.domain.journal.api

import java.util.*
import org.axonframework.commandhandling.RoutingKey

data class CreateTransactionTypeCommand(
    @RoutingKey
    val id: UUID? = null,

    val identifier: String? = null,

    val name: String? = null,

    val description: String? = null
)
