package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerAccountDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(LedgerAccountDTO::class)
        val ledgerAccountDTO1 = LedgerAccountDTO()
        ledgerAccountDTO1.id = 1L
        val ledgerAccountDTO2 = LedgerAccountDTO()
        assertThat(ledgerAccountDTO1).isNotEqualTo(ledgerAccountDTO2)
        ledgerAccountDTO2.id = ledgerAccountDTO1.id
        assertThat(ledgerAccountDTO1).isEqualTo(ledgerAccountDTO2)
        ledgerAccountDTO2.id = 2L
        assertThat(ledgerAccountDTO1).isNotEqualTo(ledgerAccountDTO2)
        ledgerAccountDTO1.id = null
        assertThat(ledgerAccountDTO1).isNotEqualTo(ledgerAccountDTO2)
    }
}
