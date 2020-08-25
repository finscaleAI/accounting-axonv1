package org.muellners.finscale.accounting.repository

import java.util.*
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountActionView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [LedgerAccountActionView] entity.
 */
@Suppress("unused")
@Repository
interface LedgerAccountActionViewRepository : JpaRepository<LedgerAccountActionView, UUID>
