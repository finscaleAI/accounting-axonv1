package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class JournalEntryDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(JournalEntryDTO::class)
        val journalEntryDTO1 = JournalEntryDTO()
        journalEntryDTO1.id = 1L
        val journalEntryDTO2 = JournalEntryDTO()
        assertThat(journalEntryDTO1).isNotEqualTo(journalEntryDTO2)
        journalEntryDTO2.id = journalEntryDTO1.id
        assertThat(journalEntryDTO1).isEqualTo(journalEntryDTO2)
        journalEntryDTO2.id = 2L
        assertThat(journalEntryDTO1).isNotEqualTo(journalEntryDTO2)
        journalEntryDTO1.id = null
        assertThat(journalEntryDTO1).isNotEqualTo(journalEntryDTO2)
    }
}
