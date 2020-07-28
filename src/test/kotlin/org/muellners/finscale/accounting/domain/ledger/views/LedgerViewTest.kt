package org.muellners.finscale.accounting.domain.ledger.views

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerViewTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(LedgerView::class)
        val ledgerView1 = LedgerView()
        ledgerView1.id = "1"
        val ledgerView2 = LedgerView()
        ledgerView2.id = ledgerView1.id
        assertThat(ledgerView1).isEqualTo(ledgerView2)
        ledgerView2.id = "2"
        assertThat(ledgerView1).isNotEqualTo(ledgerView2)
        ledgerView1.id = null
        assertThat(ledgerView1).isNotEqualTo(ledgerView2)
    }
}
