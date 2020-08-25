package org.muellners.finscale.accounting.domain.ledger.api

import java.math.BigDecimal
import java.util.*
import org.axonframework.commandhandling.RoutingKey

data class ValidateAndCreateLedgerAccountCommand(
    @RoutingKey
    val id: UUID? = null,

    val ledgerId: UUID? = null,

    val identifier: String? = null,

    val alternativeAccountNumber: String? = null,

    val name: String? = null,

    val balance: BigDecimal? = null,

    val holderIds: Set<String>? = null,

    val signatureAuthorityIds: Set<String>? = null,

    val referenceAccountId: UUID? = null

)
