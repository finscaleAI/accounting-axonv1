package org.muellners.finscale.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.domain.journal.views.JournalEntryView
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class JournalEntryTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(JournalEntryView::class)
        val journalEntry1 = JournalEntryView()
        journalEntry1.id = 1L
        val journalEntry2 = JournalEntryView()
        journalEntry2.id = journalEntry1.id
        assertThat(journalEntry1).isEqualTo(journalEntry2)
        journalEntry2.id = 2L
        assertThat(journalEntry1).isNotEqualTo(journalEntry2)
        journalEntry1.id = null
        assertThat(journalEntry1).isNotEqualTo(journalEntry2)
    }
}
