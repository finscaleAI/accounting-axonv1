package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(LedgerDTO::class)
        val ledgerViewDTO1 = LedgerDTO()
        ledgerViewDTO1.id = 1L
        val ledgerViewDTO2 = LedgerDTO()
        assertThat(ledgerViewDTO1).isNotEqualTo(ledgerViewDTO2)
        ledgerViewDTO2.id = ledgerViewDTO1.id
        assertThat(ledgerViewDTO1).isEqualTo(ledgerViewDTO2)
        ledgerViewDTO2.id = 2L
        assertThat(ledgerViewDTO1).isNotEqualTo(ledgerViewDTO2)
        ledgerViewDTO1.id = null
        assertThat(ledgerViewDTO1).isNotEqualTo(ledgerViewDTO2)
    }
}
