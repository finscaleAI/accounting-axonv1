package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class TransactionDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(TransactionDTO::class)
        val transactionDTO1 = TransactionDTO()
        transactionDTO1.id = 1L
        val transactionDTO2 = TransactionDTO()
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2)
        transactionDTO2.id = transactionDTO1.id
        assertThat(transactionDTO1).isEqualTo(transactionDTO2)
        transactionDTO2.id = 2L
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2)
        transactionDTO1.id = null
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2)
    }
}
