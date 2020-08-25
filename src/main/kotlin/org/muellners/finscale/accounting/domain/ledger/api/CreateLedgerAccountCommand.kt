package org.muellners.finscale.accounting.domain.ledger.api

import java.math.BigDecimal
import java.util.*
import org.axonframework.commandhandling.RoutingKey
import org.muellners.finscale.accounting.domain.enumeration.LedgerType

data class CreateLedgerAccountCommand(
    @RoutingKey
    val id: UUID? = null,

    val ledgerId: UUID? = null,

    val type: LedgerType,

    val identifier: String? = null,

    val alternativeAccountNumber: String? = null,

    val name: String? = null,

    val balance: BigDecimal? = null,

    val holderIds: Set<String>? = null,

    val signatureAuthorityIds: Set<String>? = null,

    val referenceAccountId: UUID? = null

)
