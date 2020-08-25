package org.muellners.finscale.accounting.repository

import java.util.*
import org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [TransactionTypeView] entity.
 */
@Suppress("unused")
@Repository
interface TransactionTypeViewRepository : JpaRepository<TransactionTypeView, UUID> {
    fun findByIdentifier(identifier: String): Optional<TransactionTypeView>
}
