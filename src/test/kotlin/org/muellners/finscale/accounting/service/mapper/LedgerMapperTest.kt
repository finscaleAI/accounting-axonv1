package org.muellners.finscale.accounting.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LedgerMapperTest {

    private lateinit var ledgerMapper: LedgerMapper

    @BeforeEach
    fun setUp() {
        ledgerMapper = LedgerMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(ledgerMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(ledgerMapper.fromId(null)).isNull()
    }
}
