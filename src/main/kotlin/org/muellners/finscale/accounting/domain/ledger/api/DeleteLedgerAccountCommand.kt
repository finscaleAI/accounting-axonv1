package org.muellners.finscale.accounting.domain.ledger.api

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class DeleteLedgerAccountCommand(
    @TargetAggregateIdentifier
    val accountId: UUID? = null

)
