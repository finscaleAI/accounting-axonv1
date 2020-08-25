package org.muellners.finscale.accounting.domain.ledger.commands

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import org.axonframework.modelling.command.EntityId
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide

class LedgerAccountEntry() {

    @EntityId
    lateinit var entryId: UUID

    lateinit var transactionId: UUID

    lateinit var transactionSide: TransactionSide

    lateinit var amount: BigDecimal

    lateinit var balance: BigDecimal

    lateinit var processedOn: LocalDate
}
