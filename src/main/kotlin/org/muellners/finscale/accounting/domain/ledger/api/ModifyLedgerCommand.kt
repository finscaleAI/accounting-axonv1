package org.muellners.finscale.accounting.domain.ledger.api

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class ModifyLedgerCommand(
    @TargetAggregateIdentifier
    val id: UUID? = null,

    val name: String? = null,

    val description: String? = null,

    val showAccountsInChart: Boolean? = null

)
