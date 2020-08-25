package org.muellners.finscale.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(LedgerView::class)
        val ledger1 = LedgerView()
        ledger1.id = 1L
        val ledger2 = LedgerView()
        ledger2.id = ledger1.id
        assertThat(ledger1).isEqualTo(ledger2)
        ledger2.id = 2L
        assertThat(ledger1).isNotEqualTo(ledger2)
        ledger1.id = null
        assertThat(ledger1).isNotEqualTo(ledger2)
    }
}
