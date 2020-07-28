package org.muellners.finscale.accounting.domain.ledger.commands

import java.math.BigDecimal
import java.util.*
import org.axonframework.commandhandling.RoutingKey

data class CreateLedgerCommand(
    @RoutingKey
    val id: UUID? = null,

    val identifier: String? = null,

    val name: String? = null,

    val type: String? = null,

    val description: String? = null,

    val totalValue: BigDecimal? = null,

    val showAccountsInChart: Boolean? = null,

    val parentLedgerId: UUID? = null

)
