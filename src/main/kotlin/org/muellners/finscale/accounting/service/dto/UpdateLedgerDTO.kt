package org.muellners.finscale.accounting.service.dto

import java.io.Serializable
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.LedgerView] entity.
 */
data class UpdateLedgerDTO(

    var id: UUID? = null,

    @get: NotNull
    var name: String? = null,

    var description: String? = null,

    var showAccountsInChart: Boolean? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UpdateLedgerDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}
