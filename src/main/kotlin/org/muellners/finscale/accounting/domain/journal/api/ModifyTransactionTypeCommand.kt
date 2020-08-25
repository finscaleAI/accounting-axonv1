package org.muellners.finscale.accounting.domain.journal.api

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class ModifyTransactionTypeCommand(
    @TargetAggregateIdentifier
    val id: UUID? = null,

    val name: String? = null,

    val description: String? = null

)
