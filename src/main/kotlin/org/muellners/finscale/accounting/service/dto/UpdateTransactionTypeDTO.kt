package org.muellners.finscale.accounting.service.dto

import javax.validation.constraints.NotNull

/**
 * An update DTO for the [org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView] entity.
 */
data class UpdateTransactionTypeDTO(

    @get: NotNull
    var name: String? = null,

    var description: String? = null

)
