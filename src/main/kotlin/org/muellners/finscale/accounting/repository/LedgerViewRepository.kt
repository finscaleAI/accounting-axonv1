package org.muellners.finscale.accounting.repository

import java.util.*
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [LedgerView] entity.
 */
@Suppress("unused")
@Repository
interface LedgerViewRepository : JpaRepository<LedgerView, UUID> {
    fun findByIdentifier(identifier: String): Optional<LedgerView>
}
