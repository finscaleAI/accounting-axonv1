package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerAccountActionDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(LedgerAccountActionDTO::class)
        val ledgerAccountActionDTO1 = LedgerAccountActionDTO()
        ledgerAccountActionDTO1.id = 1L
        val ledgerAccountActionDTO2 = LedgerAccountActionDTO()
        assertThat(ledgerAccountActionDTO1).isNotEqualTo(ledgerAccountActionDTO2)
        ledgerAccountActionDTO2.id = ledgerAccountActionDTO1.id
        assertThat(ledgerAccountActionDTO1).isEqualTo(ledgerAccountActionDTO2)
        ledgerAccountActionDTO2.id = 2L
        assertThat(ledgerAccountActionDTO1).isNotEqualTo(ledgerAccountActionDTO2)
        ledgerAccountActionDTO1.id = null
        assertThat(ledgerAccountActionDTO1).isNotEqualTo(ledgerAccountActionDTO2)
    }
}
