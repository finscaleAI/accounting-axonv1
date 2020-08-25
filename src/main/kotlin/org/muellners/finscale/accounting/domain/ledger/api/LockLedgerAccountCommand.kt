package org.muellners.finscale.accounting.domain.ledger.api

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class LockLedgerAccountCommand(
    @TargetAggregateIdentifier
    val id: UUID? = null,

    val comment: String? = null

)