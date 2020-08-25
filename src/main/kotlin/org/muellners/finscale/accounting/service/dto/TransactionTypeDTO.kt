package org.muellners.finscale.accounting.service.dto

import java.io.Serializable
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.TransactionType] entity.
 */
data class TransactionTypeDTO(

    var id: UUID? = null,

    @get: NotNull
    var identifier: String? = null,

    @get: NotNull
    var name: String? = null,

    var description: String? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionTypeDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}
