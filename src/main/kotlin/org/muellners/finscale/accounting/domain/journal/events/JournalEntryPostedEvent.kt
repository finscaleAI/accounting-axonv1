package org.muellners.finscale.accounting.domain.journal.events

import java.time.LocalDate
import java.util.*
import org.muellners.finscale.accounting.domain.enumeration.TransactionState
import org.muellners.finscale.accounting.domain.journal.commands.Transaction

data class JournalEntryPostedEvent(
    val id: UUID,

    val transactionDate: LocalDate,

    val transactionState: TransactionState,

    val transactionTypeId: UUID,

    val transactions: MutableSet<Transaction>,

    val note: String?,

    val message: String?

)
