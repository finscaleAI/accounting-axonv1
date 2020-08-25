package org.muellners.finscale.accounting.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LedgerAccountActionMapperTest {

    private lateinit var ledgerAccountActionMapper: LedgerAccountActionMapper

    @BeforeEach
    fun setUp() {
        ledgerAccountActionMapper = LedgerAccountActionMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(ledgerAccountActionMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(ledgerAccountActionMapper.fromId(null)).isNull()
    }
}
