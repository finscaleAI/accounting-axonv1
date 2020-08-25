package org.muellners.finscale.accounting.service.dto

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.LedgerAccountEntry] entity.
 */
data class LedgerAccountEntryDTO(

    var id: Long? = null,

    @get: NotNull
    var side: TransactionSide? = null,

    @get: NotNull
    var amount: BigDecimal? = null,

    @get: NotNull
    var balance: BigDecimal? = null,

    @get: NotNull
    var processedOn: LocalDate? = null,

    var transactionId: Long? = null,

    var accountId: Long? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerAccountEntryDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}
