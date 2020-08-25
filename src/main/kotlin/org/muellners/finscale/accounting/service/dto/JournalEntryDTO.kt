package org.muellners.finscale.accounting.service.dto

import java.io.Serializable
import java.time.LocalDate
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.JournalEntry] entity.
 */
data class JournalEntryDTO(

    var id: Long? = null,

    @get: NotNull
    var transactionDate: LocalDate? = null,

    var note: String? = null,

    var message: String? = null,

    var transactionTypeId: UUID? = null,

    @get: Valid
    val debtors: MutableSet<TransactionDTO>? = null,

    @get: Valid
    val creditors: MutableSet<TransactionDTO>? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JournalEntryDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}
