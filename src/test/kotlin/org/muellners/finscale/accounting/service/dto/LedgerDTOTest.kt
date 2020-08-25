package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(LedgerDTO::class)
        val ledgerDTO1 = LedgerDTO()
        ledgerDTO1.id = 1L
        val ledgerDTO2 = LedgerDTO()
        assertThat(ledgerDTO1).isNotEqualTo(ledgerDTO2)
        ledgerDTO2.id = ledgerDTO1.id
        assertThat(ledgerDTO1).isEqualTo(ledgerDTO2)
        ledgerDTO2.id = 2L
        assertThat(ledgerDTO1).isNotEqualTo(ledgerDTO2)
        ledgerDTO1.id = null
        assertThat(ledgerDTO1).isNotEqualTo(ledgerDTO2)
    }
}
