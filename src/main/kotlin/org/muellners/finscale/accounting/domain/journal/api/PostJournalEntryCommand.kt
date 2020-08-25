package org.muellners.finscale.accounting.domain.journal.api

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap
import org.axonframework.commandhandling.RoutingKey

data class PostJournalEntryCommand(
    @RoutingKey
    val id: UUID? = null,

    val transactionTypeId: UUID? = null,

    val transactionDate: LocalDate? = null,

    val debtors: HashMap<UUID, BigDecimal> = hashMapOf(),

    val creditors: HashMap<UUID, BigDecimal> = hashMapOf(),

    val note: String? = null,

    val message: String? = null

)
