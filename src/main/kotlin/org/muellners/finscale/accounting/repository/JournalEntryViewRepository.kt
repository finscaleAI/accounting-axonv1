package org.muellners.finscale.accounting.repository

import java.util.*
import org.muellners.finscale.accounting.domain.journal.views.JournalEntryView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [JournalEntryView] entity.
 */
@Suppress("unused")
@Repository
interface JournalEntryViewRepository : JpaRepository<JournalEntryView, UUID>
