package org.muellners.finscale.accounting.domain.ledger.events

import java.math.BigDecimal
import java.util.*
import org.muellners.finscale.accounting.domain.enumeration.AccountState
import org.muellners.finscale.accounting.domain.enumeration.LedgerType

data class LedgerAccountCreatedEvent(
    val ledgerAccountId: String,

    val ledgerId: UUID,

    val identifier: String,

    val alternativeAccountNumber: String? = null,

    val name: String,

    val type: LedgerType,

    val state: AccountState,

    val balance: BigDecimal,

    val holderIds: MutableList<String>,

    val signatureAuthorityIds: MutableList<String>,

    val referenceAccountId: UUID? = null

)
