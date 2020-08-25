package org.muellners.finscale.accounting.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionTypeMapperTest {

    private lateinit var transactionTypeMapper: TransactionTypeMapper

    @BeforeEach
    fun setUp() {
        transactionTypeMapper = TransactionTypeMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(transactionTypeMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(transactionTypeMapper.fromId(null)).isNull()
    }
}
