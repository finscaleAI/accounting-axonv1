package org.muellners.finscale.accounting.domain.ledger.api

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class DeleteLedgerCommand(
    @TargetAggregateIdentifier
    val id: UUID

)
