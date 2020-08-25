package org.muellners.finscale.accounting.domain.ledger.commands

import java.time.ZonedDateTime
import java.util.*
import org.axonframework.modelling.command.EntityId

class LedgerAccountAction() {

    @EntityId
    lateinit var actionId: UUID

    var comment: String? = null

    lateinit var occurredAt: ZonedDateTime
}
