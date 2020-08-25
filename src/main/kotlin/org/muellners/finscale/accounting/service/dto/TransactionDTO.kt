package org.muellners.finscale.accounting.service.dto

import java.math.BigDecimal
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the [org.muellners.finscale.accounting.domain.Transaction] entity.
 */
data class TransactionDTO(

    @get: NotNull
    var amount: BigDecimal? = null,

    @get: NotNull
    var accountId: UUID? = null

)
