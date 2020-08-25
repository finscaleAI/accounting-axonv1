package org.muellners.finscale.accounting.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionMapperTest {

    private lateinit var transactionMapper: TransactionMapper

    @BeforeEach
    fun setUp() {
        transactionMapper = TransactionMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(transactionMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(transactionMapper.fromId(null)).isNull()
    }
}
