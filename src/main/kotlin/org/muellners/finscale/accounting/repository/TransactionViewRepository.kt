package org.muellners.finscale.accounting.repository

import java.util.*
import org.muellners.finscale.accounting.domain.journal.views.TransactionView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [TransactionView] entity.
 */
@Suppress("unused")
@Repository
interface TransactionViewRepository : JpaRepository<TransactionView, UUID>
