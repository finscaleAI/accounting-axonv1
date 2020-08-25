package org.muellners.finscale.accounting.service.dto

import javax.validation.constraints.NotNull

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.LedgerView] entity.
 */
data class UpdateLedgerDTO(

    @get: NotNull
    var name: String? = null,

    var description: String? = null,

    var showAccountsInChart: Boolean? = null

)
