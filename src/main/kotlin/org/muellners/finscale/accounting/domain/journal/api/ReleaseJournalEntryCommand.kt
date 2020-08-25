package org.muellners.finscale.accounting.domain.journal.api

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class ReleaseJournalEntryCommand(
    @TargetAggregateIdentifier
    val id: UUID? = null

)
