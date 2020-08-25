package org.muellners.finscale.accounting.web.rest

import java.time.LocalDate
import java.time.ZoneId
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.muellners.finscale.accounting.AccountingApp
import org.muellners.finscale.accounting.config.SecurityBeanOverrideConfiguration
import org.muellners.finscale.accounting.domain.enumeration.TransactionState
import org.muellners.finscale.accounting.domain.journal.views.JournalEntryView
import org.muellners.finscale.accounting.repository.JournalEntryViewRepository
import org.muellners.finscale.accounting.service.JournalEntryService
import org.muellners.finscale.accounting.service.mapper.JournalEntryMapper
import org.muellners.finscale.accounting.web.rest.errors.ExceptionTranslator
import org.muellners.finscale.accounting.web.rest.witherror.JournalEntryResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator

/**
 * Integration tests for the [JournalEntryResource] REST controller.
 *
 * @see JournalEntryResource
 */
@SpringBootTest(classes = [SecurityBeanOverrideConfiguration::class, AccountingApp::class])
@AutoConfigureMockMvc
@WithMockUser
class JournalEntryResourceIT {

    @Autowired
    private lateinit var journalEntryRepository: JournalEntryViewRepository

    @Autowired
    private lateinit var journalEntryMapper: JournalEntryMapper

    @Autowired
    private lateinit var journalEntryService: JournalEntryService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    private lateinit var restJournalEntryMockMvc: MockMvc

    private lateinit var journalEntry: JournalEntryView

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val journalEntryResource = JournalEntryResource(journalEntryService)
         this.restJournalEntryMockMvc = MockMvcBuilders.standaloneSetup(journalEntryResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        journalEntry = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createJournalEntry() {
        val databaseSizeBeforeCreate = journalEntryRepository.findAll().size

        // Create the JournalEntry
        val journalEntryDTO = journalEntryMapper.toDto(journalEntry)
        restJournalEntryMockMvc.perform(
            post("/api/journal-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(journalEntryDTO))
        ).andExpect(status().isCreated)

        // Validate the JournalEntry in the database
        val journalEntryList = journalEntryRepository.findAll()
        assertThat(journalEntryList).hasSize(databaseSizeBeforeCreate + 1)
        val testJournalEntry = journalEntryList[journalEntryList.size - 1]
        assertThat(testJournalEntry.transactionDate).isEqualTo(DEFAULT_TRANSACTION_DATE)
        assertThat(testJournalEntry.transactionState).isEqualTo(DEFAULT_TRANSACTION_STATE)
        assertThat(testJournalEntry.note).isEqualTo(DEFAULT_NOTE)
        assertThat(testJournalEntry.message).isEqualTo(DEFAULT_MESSAGE)
    }

    @Test
    @Transactional
    fun createJournalEntryWithExistingId() {
        val databaseSizeBeforeCreate = journalEntryRepository.findAll().size

        // Create the JournalEntry with an existing ID
        journalEntry.id = 1L
        val journalEntryDTO = journalEntryMapper.toDto(journalEntry)

        // An entity with an existing ID cannot be created, so this API call must fail
        restJournalEntryMockMvc.perform(
            post("/api/journal-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(journalEntryDTO))
        ).andExpect(status().isBadRequest)

        // Validate the JournalEntry in the database
        val journalEntryList = journalEntryRepository.findAll()
        assertThat(journalEntryList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkTransactionDateIsRequired() {
        val databaseSizeBeforeTest = journalEntryRepository.findAll().size
        // set the field null
        journalEntry.transactionDate = null

        // Create the JournalEntry, which fails.
        val journalEntryDTO = journalEntryMapper.toDto(journalEntry)

        restJournalEntryMockMvc.perform(
            post("/api/journal-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(journalEntryDTO))
        ).andExpect(status().isBadRequest)

        val journalEntryList = journalEntryRepository.findAll()
        assertThat(journalEntryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkTransactionStateIsRequired() {
        val databaseSizeBeforeTest = journalEntryRepository.findAll().size
        // set the field null
        journalEntry.transactionState = null

        // Create the JournalEntry, which fails.
        val journalEntryDTO = journalEntryMapper.toDto(journalEntry)

        restJournalEntryMockMvc.perform(
            post("/api/journal-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(journalEntryDTO))
        ).andExpect(status().isBadRequest)

        val journalEntryList = journalEntryRepository.findAll()
        assertThat(journalEntryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllJournalEntries() {
        // Initialize the database
        journalEntryRepository.saveAndFlush(journalEntry)

        // Get all the journalEntryList
        restJournalEntryMockMvc.perform(get("/api/journal-entries?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(journalEntry.id?.toInt())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(DEFAULT_TRANSACTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].transactionState").value(hasItem(DEFAULT_TRANSACTION_STATE.toString())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getJournalEntry() {
        // Initialize the database
        journalEntryRepository.saveAndFlush(journalEntry)

        val id = journalEntry.id
        assertNotNull(id)

        // Get the journalEntry
        restJournalEntryMockMvc.perform(get("/api/journal-entries/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(journalEntry.id?.toInt()))
            .andExpect(jsonPath("$.transactionDate").value(DEFAULT_TRANSACTION_DATE.toString()))
            .andExpect(jsonPath("$.transactionState").value(DEFAULT_TRANSACTION_STATE.toString()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE)) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingJournalEntry() {
        // Get the journalEntry
        restJournalEntryMockMvc.perform(get("/api/journal-entries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateJournalEntry() {
        // Initialize the database
        journalEntryRepository.saveAndFlush(journalEntry)

        val databaseSizeBeforeUpdate = journalEntryRepository.findAll().size

        // Update the journalEntry
        val id = journalEntry.id
        assertNotNull(id)
        val updatedJournalEntry = journalEntryRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedJournalEntry are not directly saved in db
        em.detach(updatedJournalEntry)
        updatedJournalEntry.transactionDate = UPDATED_TRANSACTION_DATE
        updatedJournalEntry.transactionState = UPDATED_TRANSACTION_STATE
        updatedJournalEntry.note = UPDATED_NOTE
        updatedJournalEntry.message = UPDATED_MESSAGE
        val journalEntryDTO = journalEntryMapper.toDto(updatedJournalEntry)

        restJournalEntryMockMvc.perform(
            put("/api/journal-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(journalEntryDTO))
        ).andExpect(status().isOk)

        // Validate the JournalEntry in the database
        val journalEntryList = journalEntryRepository.findAll()
        assertThat(journalEntryList).hasSize(databaseSizeBeforeUpdate)
        val testJournalEntry = journalEntryList[journalEntryList.size - 1]
        assertThat(testJournalEntry.transactionDate).isEqualTo(UPDATED_TRANSACTION_DATE)
        assertThat(testJournalEntry.transactionState).isEqualTo(UPDATED_TRANSACTION_STATE)
        assertThat(testJournalEntry.note).isEqualTo(UPDATED_NOTE)
        assertThat(testJournalEntry.message).isEqualTo(UPDATED_MESSAGE)
    }

    @Test
    @Transactional
    fun updateNonExistingJournalEntry() {
        val databaseSizeBeforeUpdate = journalEntryRepository.findAll().size

        // Create the JournalEntry
        val journalEntryDTO = journalEntryMapper.toDto(journalEntry)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJournalEntryMockMvc.perform(
            put("/api/journal-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(journalEntryDTO))
        ).andExpect(status().isBadRequest)

        // Validate the JournalEntry in the database
        val journalEntryList = journalEntryRepository.findAll()
        assertThat(journalEntryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteJournalEntry() {
        // Initialize the database
        journalEntryRepository.saveAndFlush(journalEntry)

        val databaseSizeBeforeDelete = journalEntryRepository.findAll().size

        // Delete the journalEntry
        restJournalEntryMockMvc.perform(
            delete("/api/journal-entries/{id}", journalEntry.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val journalEntryList = journalEntryRepository.findAll()
        assertThat(journalEntryList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private val DEFAULT_TRANSACTION_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_TRANSACTION_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_TRANSACTION_STATE: TransactionState = TransactionState.PENDING
        private val UPDATED_TRANSACTION_STATE: TransactionState = TransactionState.PROCESSED

        private const val DEFAULT_NOTE = "AAAAAAAAAA"
        private const val UPDATED_NOTE = "BBBBBBBBBB"

        private const val DEFAULT_MESSAGE = "AAAAAAAAAA"
        private const val UPDATED_MESSAGE = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): JournalEntryView {
            val journalEntry = JournalEntryView(
                    transactionDate = DEFAULT_TRANSACTION_DATE,
                    transactionState = DEFAULT_TRANSACTION_STATE,
                    note = DEFAULT_NOTE,
                    message = DEFAULT_MESSAGE
            )

            return journalEntry
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): JournalEntryView {
            val journalEntry = JournalEntryView(
                    transactionDate = UPDATED_TRANSACTION_DATE,
                    transactionState = UPDATED_TRANSACTION_STATE,
                    note = UPDATED_NOTE,
                    message = UPDATED_MESSAGE
            )

            return journalEntry
        }
    }
}
