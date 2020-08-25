package org.muellners.finscale.accounting.service.dto

import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import javax.validation.constraints.NotNull
import org.muellners.finscale.accounting.domain.enumeration.LedgerType

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.Ledger] entity.
 */
data class LedgerDTO(

    var id: UUID? = null,

    @get: NotNull
    var identifier: String? = null,

    @get: NotNull
    var name: String? = null,

    @get: NotNull
    var type: LedgerType? = null,

    var description: String? = null,

    var totalValue: BigDecimal? = null,

    @get: NotNull
    var showAccountsInChart: Boolean? = null,

    var parentLedgerId: UUID? = null,

    var ledgerId: Long? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}
