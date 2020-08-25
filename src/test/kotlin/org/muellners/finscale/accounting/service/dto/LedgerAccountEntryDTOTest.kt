package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerAccountEntryDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(LedgerAccountEntryDTO::class)
        val ledgerAccountEntryDTO1 = LedgerAccountEntryDTO()
        ledgerAccountEntryDTO1.id = 1L
        val ledgerAccountEntryDTO2 = LedgerAccountEntryDTO()
        assertThat(ledgerAccountEntryDTO1).isNotEqualTo(ledgerAccountEntryDTO2)
        ledgerAccountEntryDTO2.id = ledgerAccountEntryDTO1.id
        assertThat(ledgerAccountEntryDTO1).isEqualTo(ledgerAccountEntryDTO2)
        ledgerAccountEntryDTO2.id = 2L
        assertThat(ledgerAccountEntryDTO1).isNotEqualTo(ledgerAccountEntryDTO2)
        ledgerAccountEntryDTO1.id = null
        assertThat(ledgerAccountEntryDTO1).isNotEqualTo(ledgerAccountEntryDTO2)
    }
}
