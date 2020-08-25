package org.muellners.finscale.accounting.service.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.muellners.finscale.accounting.web.rest.equalsVerifier

class TransactionTypeDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(TransactionTypeDTO::class)
        val transactionTypeDTO1 = TransactionTypeDTO()
        transactionTypeDTO1.id = 1L
        val transactionTypeDTO2 = TransactionTypeDTO()
        assertThat(transactionTypeDTO1).isNotEqualTo(transactionTypeDTO2)
        transactionTypeDTO2.id = transactionTypeDTO1.id
        assertThat(transactionTypeDTO1).isEqualTo(transactionTypeDTO2)
        transactionTypeDTO2.id = 2L
        assertThat(transactionTypeDTO1).isNotEqualTo(transactionTypeDTO2)
        transactionTypeDTO1.id = null
        assertThat(transactionTypeDTO1).isNotEqualTo(transactionTypeDTO2)
    }
}
