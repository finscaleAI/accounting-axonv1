package org.muellners.finscale.accounting.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LedgerAccountMapperTest {

    private lateinit var ledgerAccountMapper: LedgerAccountMapper

    @BeforeEach
    fun setUp() {
        ledgerAccountMapper = LedgerAccountMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(ledgerAccountMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(ledgerAccountMapper.fromId(null)).isNull()
    }
}
