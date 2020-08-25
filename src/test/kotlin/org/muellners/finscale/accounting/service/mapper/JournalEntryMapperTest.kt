package org.muellners.finscale.accounting.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JournalEntryMapperTest {

    private lateinit var journalEntryMapper: JournalEntryMapper

    @BeforeEach
    fun setUp() {
        journalEntryMapper = JournalEntryMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(journalEntryMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(journalEntryMapper.fromId(null)).isNull()
    }
}
