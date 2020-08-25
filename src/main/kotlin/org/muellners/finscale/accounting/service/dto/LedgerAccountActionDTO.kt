package org.muellners.finscale.accounting.service.dto

import javax.validation.constraints.NotNull

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.LedgerAccountAction] entity.
 */
data class LedgerAccountActionDTO(

    @get: NotNull
    var comment: String? = null

)
