package org.muellners.finscale.accounting.domain.journal.events

import java.util.*
import org.muellners.finscale.accounting.domain.journal.commands.Transaction

data class JournalEntryBookedEvent(
    val journalEntryId: UUID,

    val transactions: Set<Transaction> = setOf()

)
