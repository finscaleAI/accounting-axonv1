package org.muellners.finscale.accounting.web.rest

import java.math.BigDecimal
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
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountEntryView
import org.muellners.finscale.accounting.repository.LedgerAccountEntryViewRepository
import org.muellners.finscale.accounting.service.LedgerAccountEntryService
import org.muellners.finscale.accounting.service.mapper.LedgerAccountEntryMapper
import org.muellners.finscale.accounting.web.rest.errors.ExceptionTranslator
import org.muellners.finscale.accounting.web.rest.witherror.LedgerAccountEntryResource
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
 * Integration tests for the [LedgerAccountEntryResource] REST controller.
 *
 * @see LedgerAccountEntryResource
 */
@SpringBootTest(classes = [SecurityBeanOverrideConfiguration::class, AccountingApp::class])
@AutoConfigureMockMvc
@WithMockUser
class LedgerAccountEntryResourceIT {

    @Autowired
    private lateinit var ledgerAccountEntryRepository: LedgerAccountEntryViewRepository

    @Autowired
    private lateinit var ledgerAccountEntryMapper: LedgerAccountEntryMapper

    @Autowired
    private lateinit var ledgerAccountEntryService: LedgerAccountEntryService

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

    private lateinit var restLedgerAccountEntryMockMvc: MockMvc

    private lateinit var ledgerAccountEntry: LedgerAccountEntryView

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val ledgerAccountEntryResource = LedgerAccountEntryResource(ledgerAccountEntryService)
         this.restLedgerAccountEntryMockMvc = MockMvcBuilders.standaloneSetup(ledgerAccountEntryResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        ledgerAccountEntry = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createLedgerAccountEntry() {
        val databaseSizeBeforeCreate = ledgerAccountEntryRepository.findAll().size

        // Create the LedgerAccountEntry
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(ledgerAccountEntry)
        restLedgerAccountEntryMockMvc.perform(
            post("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isCreated)

        // Validate the LedgerAccountEntry in the database
        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeCreate + 1)
        val testLedgerAccountEntry = ledgerAccountEntryList[ledgerAccountEntryList.size - 1]
        assertThat(testLedgerAccountEntry.side).isEqualTo(DEFAULT_SIDE)
        assertThat(testLedgerAccountEntry.amount).isEqualTo(DEFAULT_AMOUNT)
        assertThat(testLedgerAccountEntry.balance).isEqualTo(DEFAULT_BALANCE)
        assertThat(testLedgerAccountEntry.processedOn).isEqualTo(DEFAULT_PROCESSED_ON)
    }

    @Test
    @Transactional
    fun createLedgerAccountEntryWithExistingId() {
        val databaseSizeBeforeCreate = ledgerAccountEntryRepository.findAll().size

        // Create the LedgerAccountEntry with an existing ID
        ledgerAccountEntry.id = 1L
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(ledgerAccountEntry)

        // An entity with an existing ID cannot be created, so this API call must fail
        restLedgerAccountEntryMockMvc.perform(
            post("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isBadRequest)

        // Validate the LedgerAccountEntry in the database
        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkSideIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountEntryRepository.findAll().size
        // set the field null
        ledgerAccountEntry.side = null

        // Create the LedgerAccountEntry, which fails.
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(ledgerAccountEntry)

        restLedgerAccountEntryMockMvc.perform(
            post("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkAmountIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountEntryRepository.findAll().size
        // set the field null
        ledgerAccountEntry.amount = null

        // Create the LedgerAccountEntry, which fails.
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(ledgerAccountEntry)

        restLedgerAccountEntryMockMvc.perform(
            post("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkBalanceIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountEntryRepository.findAll().size
        // set the field null
        ledgerAccountEntry.balance = null

        // Create the LedgerAccountEntry, which fails.
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(ledgerAccountEntry)

        restLedgerAccountEntryMockMvc.perform(
            post("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkProcessedOnIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountEntryRepository.findAll().size
        // set the field null
        ledgerAccountEntry.processedOn = null

        // Create the LedgerAccountEntry, which fails.
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(ledgerAccountEntry)

        restLedgerAccountEntryMockMvc.perform(
            post("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllLedgerAccountEntries() {
        // Initialize the database
        ledgerAccountEntryRepository.saveAndFlush(ledgerAccountEntry)

        // Get all the ledgerAccountEntryList
        restLedgerAccountEntryMockMvc.perform(get("/api/ledger-account-entries?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ledgerAccountEntry.id?.toInt())))
            .andExpect(jsonPath("$.[*].side").value(hasItem(DEFAULT_SIDE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT?.toInt())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE?.toInt())))
            .andExpect(jsonPath("$.[*].processedOn").value(hasItem(DEFAULT_PROCESSED_ON.toString()))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getLedgerAccountEntry() {
        // Initialize the database
        ledgerAccountEntryRepository.saveAndFlush(ledgerAccountEntry)

        val id = ledgerAccountEntry.id
        assertNotNull(id)

        // Get the ledgerAccountEntry
        restLedgerAccountEntryMockMvc.perform(get("/api/ledger-account-entries/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ledgerAccountEntry.id?.toInt()))
            .andExpect(jsonPath("$.side").value(DEFAULT_SIDE.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT?.toInt()))
            .andExpect(jsonPath("$.balance").value(DEFAULT_BALANCE?.toInt()))
            .andExpect(jsonPath("$.processedOn").value(DEFAULT_PROCESSED_ON.toString())) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingLedgerAccountEntry() {
        // Get the ledgerAccountEntry
        restLedgerAccountEntryMockMvc.perform(get("/api/ledger-account-entries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateLedgerAccountEntry() {
        // Initialize the database
        ledgerAccountEntryRepository.saveAndFlush(ledgerAccountEntry)

        val databaseSizeBeforeUpdate = ledgerAccountEntryRepository.findAll().size

        // Update the ledgerAccountEntry
        val id = ledgerAccountEntry.id
        assertNotNull(id)
        val updatedLedgerAccountEntry = ledgerAccountEntryRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedLedgerAccountEntry are not directly saved in db
        em.detach(updatedLedgerAccountEntry)
        updatedLedgerAccountEntry.side = UPDATED_SIDE
        updatedLedgerAccountEntry.amount = UPDATED_AMOUNT
        updatedLedgerAccountEntry.balance = UPDATED_BALANCE
        updatedLedgerAccountEntry.processedOn = UPDATED_PROCESSED_ON
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(updatedLedgerAccountEntry)

        restLedgerAccountEntryMockMvc.perform(
            put("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isOk)

        // Validate the LedgerAccountEntry in the database
        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeUpdate)
        val testLedgerAccountEntry = ledgerAccountEntryList[ledgerAccountEntryList.size - 1]
        assertThat(testLedgerAccountEntry.side).isEqualTo(UPDATED_SIDE)
        assertThat(testLedgerAccountEntry.amount).isEqualTo(UPDATED_AMOUNT)
        assertThat(testLedgerAccountEntry.balance).isEqualTo(UPDATED_BALANCE)
        assertThat(testLedgerAccountEntry.processedOn).isEqualTo(UPDATED_PROCESSED_ON)
    }

    @Test
    @Transactional
    fun updateNonExistingLedgerAccountEntry() {
        val databaseSizeBeforeUpdate = ledgerAccountEntryRepository.findAll().size

        // Create the LedgerAccountEntry
        val ledgerAccountEntryDTO = ledgerAccountEntryMapper.toDto(ledgerAccountEntry)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLedgerAccountEntryMockMvc.perform(
            put("/api/ledger-account-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountEntryDTO))
        ).andExpect(status().isBadRequest)

        // Validate the LedgerAccountEntry in the database
        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteLedgerAccountEntry() {
        // Initialize the database
        ledgerAccountEntryRepository.saveAndFlush(ledgerAccountEntry)

        val databaseSizeBeforeDelete = ledgerAccountEntryRepository.findAll().size

        // Delete the ledgerAccountEntry
        restLedgerAccountEntryMockMvc.perform(
            delete("/api/ledger-account-entries/{id}", ledgerAccountEntry.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val ledgerAccountEntryList = ledgerAccountEntryRepository.findAll()
        assertThat(ledgerAccountEntryList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private val DEFAULT_SIDE: TransactionSide = TransactionSide.DEBIT
        private val UPDATED_SIDE: TransactionSide = TransactionSide.CREDIT

        private val DEFAULT_AMOUNT: BigDecimal = BigDecimal(1)
        private val UPDATED_AMOUNT: BigDecimal = BigDecimal(2)

        private val DEFAULT_BALANCE: BigDecimal = BigDecimal(1)
        private val UPDATED_BALANCE: BigDecimal = BigDecimal(2)

        private val DEFAULT_PROCESSED_ON: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_PROCESSED_ON: LocalDate = LocalDate.now(ZoneId.systemDefault())

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): LedgerAccountEntryView {
            val ledgerAccountEntry = LedgerAccountEntryView(
                    side = DEFAULT_SIDE,
                    amount = DEFAULT_AMOUNT,
                    balance = DEFAULT_BALANCE,
                    processedOn = DEFAULT_PROCESSED_ON
            )

            return ledgerAccountEntry
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): LedgerAccountEntryView {
            val ledgerAccountEntry = LedgerAccountEntryView(
                    side = UPDATED_SIDE,
                    amount = UPDATED_AMOUNT,
                    balance = UPDATED_BALANCE,
                    processedOn = UPDATED_PROCESSED_ON
            )

            return ledgerAccountEntry
        }
    }
}
