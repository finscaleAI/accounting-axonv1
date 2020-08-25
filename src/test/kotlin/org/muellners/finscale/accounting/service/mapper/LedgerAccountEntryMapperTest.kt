package org.muellners.finscale.accounting.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LedgerAccountEntryMapperTest {

    private lateinit var ledgerAccountEntryMapper: LedgerAccountEntryMapper

    @BeforeEach
    fun setUp() {
        ledgerAccountEntryMapper = LedgerAccountEntryMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(ledgerAccountEntryMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(ledgerAccountEntryMapper.fromId(null)).isNull()
    }
}
