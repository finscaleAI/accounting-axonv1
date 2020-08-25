package org.muellners.finscale.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.domain.journal.views.TransactionView
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class TransactionTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(TransactionView::class)
        val transaction1 = TransactionView()
        transaction1.id = 1L
        val transaction2 = TransactionView()
        transaction2.id = transaction1.id
        assertThat(transaction1).isEqualTo(transaction2)
        transaction2.id = 2L
        assertThat(transaction1).isNotEqualTo(transaction2)
        transaction1.id = null
        assertThat(transaction1).isNotEqualTo(transaction2)
    }
}
