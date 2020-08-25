package org.muellners.finscale.accounting.domain.ledger.events

import java.time.ZonedDateTime
import java.util.*
import org.muellners.finscale.accounting.domain.enumeration.AccountAction
import org.muellners.finscale.accounting.domain.enumeration.AccountState

data class LedgerAccountActionPerformedEvent(
    val accountId: UUID,

    val actionId: UUID,

    val action: AccountAction,

    val accountState: AccountState,

    val comment: String? = null,

    val occurredAt: ZonedDateTime

)
