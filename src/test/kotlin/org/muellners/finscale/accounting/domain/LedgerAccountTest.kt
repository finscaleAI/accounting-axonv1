package org.muellners.finscale.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerAccountTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(LedgerAccountView::class)
        val ledgerAccount1 = LedgerAccountView()
        ledgerAccount1.id = 1L
        val ledgerAccount2 = LedgerAccountView()
        ledgerAccount2.id = ledgerAccount1.id
        assertThat(ledgerAccount1).isEqualTo(ledgerAccount2)
        ledgerAccount2.id = 2L
        assertThat(ledgerAccount1).isNotEqualTo(ledgerAccount2)
        ledgerAccount1.id = null
        assertThat(ledgerAccount1).isNotEqualTo(ledgerAccount2)
    }
}
