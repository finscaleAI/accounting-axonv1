package org.muellners.finscale.accounting.repository

import java.util.*
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountEntryView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [LedgerAccountEntryView] entity.
 */
@Suppress("unused")
@Repository
interface LedgerAccountEntryViewRepository : JpaRepository<LedgerAccountEntryView, UUID>
