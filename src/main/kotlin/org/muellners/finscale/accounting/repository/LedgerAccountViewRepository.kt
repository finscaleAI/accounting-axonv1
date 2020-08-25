package org.muellners.finscale.accounting.repository

import java.util.*
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [LedgerAccountView] entity.
 */
@Suppress("unused")
@Repository
interface LedgerAccountViewRepository : JpaRepository<LedgerAccountView, UUID> {
    fun findByIdentifier(identifier: String): Optional<LedgerAccountView>
}
