package org.muellners.finscale.accounting.domain.ledger.commands

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class DeleteLedgerCommand(
    @TargetAggregateIdentifier
    val id: UUID

)
