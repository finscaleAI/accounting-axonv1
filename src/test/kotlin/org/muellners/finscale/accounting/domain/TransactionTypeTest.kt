package org.muellners.finscale.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class TransactionTypeTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(TransactionTypeView::class)
        val transactionType1 = TransactionTypeView()
        transactionType1.id = 1L
        val transactionType2 = TransactionTypeView()
        transactionType2.id = transactionType1.id
        assertThat(transactionType1).isEqualTo(transactionType2)
        transactionType2.id = 2L
        assertThat(transactionType1).isNotEqualTo(transactionType2)
        transactionType1.id = null
        assertThat(transactionType1).isNotEqualTo(transactionType2)
    }
}
