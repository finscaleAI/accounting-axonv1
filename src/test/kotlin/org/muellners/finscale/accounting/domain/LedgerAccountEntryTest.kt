package org.muellners.finscale.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountEntryView
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class LedgerAccountEntryTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(LedgerAccountEntryView::class)
        val ledgerAccountEntry1 = LedgerAccountEntryView()
        ledgerAccountEntry1.id = 1L
        val ledgerAccountEntry2 = LedgerAccountEntryView()
        ledgerAccountEntry2.id = ledgerAccountEntry1.id
        assertThat(ledgerAccountEntry1).isEqualTo(ledgerAccountEntry2)
        ledgerAccountEntry2.id = 2L
        assertThat(ledgerAccountEntry1).isNotEqualTo(ledgerAccountEntry2)
        ledgerAccountEntry1.id = null
        assertThat(ledgerAccountEntry1).isNotEqualTo(ledgerAccountEntry2)
    }
}
