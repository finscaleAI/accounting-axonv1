package org.muellners.finscale.accounting.domain.journal.events

import java.util.*

data class JournalEntryReleasedEvent(
    val journalEntryId: UUID

)
