package org.muellners.finscale.accounting.domain.journal.commands

import java.math.BigDecimal
import java.util.*
import org.axonframework.modelling.command.EntityId
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide

class Transaction() {

    @EntityId
    lateinit var transactionId: UUID

    lateinit var accountId: UUID

    lateinit var transactionSide: TransactionSide

    lateinit var amount: BigDecimal
}
