package org.muellners.finscale.accounting.domain.ledger.api

import java.math.BigDecimal
import java.util.*
import org.axonframework.commandhandling.RoutingKey
import org.muellners.finscale.accounting.domain.enumeration.LedgerType

data class CreateLedgerCommand(
    @RoutingKey
    val id: UUID? = null,

    val identifier: String? = null,

    val name: String? = null,

    val type: LedgerType? = null,

    val description: String? = null,

    val totalValue: BigDecimal? = null,

    val showAccountsInChart: Boolean? = null,

    val parentLedgerId: UUID? = null

)
