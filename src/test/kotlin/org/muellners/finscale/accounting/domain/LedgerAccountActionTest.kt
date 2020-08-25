package org.muellners.finscale.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountActionView
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerAccountActionTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(LedgerAccountActionView::class)
        val ledgerAccountAction1 = LedgerAccountActionView()
        ledgerAccountAction1.id = 1L
        val ledgerAccountAction2 = LedgerAccountActionView()
        ledgerAccountAction2.id = ledgerAccountAction1.id
        assertThat(ledgerAccountAction1).isEqualTo(ledgerAccountAction2)
        ledgerAccountAction2.id = 2L
        assertThat(ledgerAccountAction1).isNotEqualTo(ledgerAccountAction2)
        ledgerAccountAction1.id = null
        assertThat(ledgerAccountAction1).isNotEqualTo(ledgerAccountAction2)
    }
}
