package org.muellners.finscale.accounting.service.dto

import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.LedgerAccount] entity.
 */
data class LedgerAccountDTO(

    var id: UUID? = null,

    @get: NotNull
    var identifier: String? = null,

    var alternativeAccountNumber: String? = null,

    @get: NotNull
    var name: String? = null,

    @get: NotNull
    var holders: Set<String>? = null,

    @get: NotNull
    var signatureAuthorities: Set<String>? = null,

    var balance: BigDecimal? = null,

    var ledgerId: UUID? = null,

    var referenceAccountId: UUID? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerAccountDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}
