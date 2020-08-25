package org.muellners.finscale.accounting.domain.accounting.api

import java.util.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class BookJournalEntryCommand(
    @TargetAggregateIdentifier
    val id: UUID? = null

)
