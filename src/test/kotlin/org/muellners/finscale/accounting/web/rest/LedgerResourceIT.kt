package org.muellners.finscale.accounting.web.rest

import java.math.BigDecimal
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.muellners.finscale.accounting.AccountingApp
import org.muellners.finscale.accounting.config.SecurityBeanOverrideConfiguration
import org.muellners.finscale.accounting.domain.enumeration.LedgerType
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.muellners.finscale.accounting.service.LedgerService
import org.muellners.finscale.accounting.service.mapper.LedgerMapper
import org.muellners.finscale.accounting.web.rest.errors.ExceptionTranslator
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
 * Integration tests for the [LedgerResource] REST controller.
 *
 * @see LedgerResource
 */
@SpringBootTest(classes = [SecurityBeanOverrideConfiguration::class, AccountingApp::class])
@AutoConfigureMockMvc
@WithMockUser
class LedgerResourceIT {

    @Autowired
    private lateinit var ledgerRepository: LedgerRepository

    @Autowired
    private lateinit var ledgerMapper: LedgerMapper

    @Autowired
    private lateinit var ledgerService: LedgerService

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

    private lateinit var restLedgerMockMvc: MockMvc

    private lateinit var ledger: LedgerView

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val ledgerResource = LedgerResource(ledgerService)
         this.restLedgerMockMvc = MockMvcBuilders.standaloneSetup(ledgerResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        ledger = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createLedger() {
        val databaseSizeBeforeCreate = ledgerRepository.findAll().size

        // Create the Ledger
        val ledgerDTO = ledgerMapper.toDto(ledger)
        restLedgerMockMvc.perform(
            post("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isCreated)

        // Validate the Ledger in the database
        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeCreate + 1)
        val testLedger = ledgerList[ledgerList.size - 1]
        assertThat(testLedger.identifier).isEqualTo(DEFAULT_IDENTIFIER)
        assertThat(testLedger.name).isEqualTo(DEFAULT_NAME)
        assertThat(testLedger.type).isEqualTo(DEFAULT_TYPE)
        assertThat(testLedger.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testLedger.totalValue).isEqualTo(DEFAULT_TOTAL_VALUE)
        assertThat(testLedger.showAccountsInChart).isEqualTo(DEFAULT_SHOW_ACCOUNTS_IN_CHART)
    }

    @Test
    @Transactional
    fun createLedgerWithExistingId() {
        val databaseSizeBeforeCreate = ledgerRepository.findAll().size

        // Create the Ledger with an existing ID
        ledger.id = 1L
        val ledgerDTO = ledgerMapper.toDto(ledger)

        // An entity with an existing ID cannot be created, so this API call must fail
        restLedgerMockMvc.perform(
            post("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Ledger in the database
        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkIdentifierIsRequired() {
        val databaseSizeBeforeTest = ledgerRepository.findAll().size
        // set the field null
        ledger.identifier = null

        // Create the Ledger, which fails.
        val ledgerDTO = ledgerMapper.toDto(ledger)

        restLedgerMockMvc.perform(
            post("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isBadRequest)

        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = ledgerRepository.findAll().size
        // set the field null
        ledger.name = null

        // Create the Ledger, which fails.
        val ledgerDTO = ledgerMapper.toDto(ledger)

        restLedgerMockMvc.perform(
            post("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isBadRequest)

        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkTypeIsRequired() {
        val databaseSizeBeforeTest = ledgerRepository.findAll().size
        // set the field null
        ledger.type = null

        // Create the Ledger, which fails.
        val ledgerDTO = ledgerMapper.toDto(ledger)

        restLedgerMockMvc.perform(
            post("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isBadRequest)

        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkTotalValueIsRequired() {
        val databaseSizeBeforeTest = ledgerRepository.findAll().size
        // set the field null
        ledger.totalValue = null

        // Create the Ledger, which fails.
        val ledgerDTO = ledgerMapper.toDto(ledger)

        restLedgerMockMvc.perform(
            post("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isBadRequest)

        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkShowAccountsInChartIsRequired() {
        val databaseSizeBeforeTest = ledgerRepository.findAll().size
        // set the field null
        ledger.showAccountsInChart = null

        // Create the Ledger, which fails.
        val ledgerDTO = ledgerMapper.toDto(ledger)

        restLedgerMockMvc.perform(
            post("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isBadRequest)

        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllLedgers() {
        // Initialize the database
        ledgerRepository.saveAndFlush(ledger)

        // Get all the ledgerList
        restLedgerMockMvc.perform(get("/api/ledgers?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ledger.id?.toInt())))
            .andExpect(jsonPath("$.[*].identifier").value(hasItem(DEFAULT_IDENTIFIER)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].totalValue").value(hasItem(DEFAULT_TOTAL_VALUE?.toInt())))
            .andExpect(jsonPath("$.[*].showAccountsInChart").value(hasItem(DEFAULT_SHOW_ACCOUNTS_IN_CHART))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getLedger() {
        // Initialize the database
        ledgerRepository.saveAndFlush(ledger)

        val id = ledger.id
        assertNotNull(id)

        // Get the ledger
        restLedgerMockMvc.perform(get("/api/ledgers/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ledger.id?.toInt()))
            .andExpect(jsonPath("$.identifier").value(DEFAULT_IDENTIFIER))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.totalValue").value(DEFAULT_TOTAL_VALUE?.toInt()))
            .andExpect(jsonPath("$.showAccountsInChart").value(DEFAULT_SHOW_ACCOUNTS_IN_CHART)) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingLedger() {
        // Get the ledger
        restLedgerMockMvc.perform(get("/api/ledgers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateLedger() {
        // Initialize the database
        ledgerRepository.saveAndFlush(ledger)

        val databaseSizeBeforeUpdate = ledgerRepository.findAll().size

        // Update the ledger
        val id = ledger.id
        assertNotNull(id)
        val updatedLedger = ledgerRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedLedger are not directly saved in db
        em.detach(updatedLedger)
        updatedLedger.identifier = UPDATED_IDENTIFIER
        updatedLedger.name = UPDATED_NAME
        updatedLedger.type = UPDATED_TYPE
        updatedLedger.description = UPDATED_DESCRIPTION
        updatedLedger.totalValue = UPDATED_TOTAL_VALUE
        updatedLedger.showAccountsInChart = UPDATED_SHOW_ACCOUNTS_IN_CHART
        val ledgerDTO = ledgerMapper.toDto(updatedLedger)

        restLedgerMockMvc.perform(
            put("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isOk)

        // Validate the Ledger in the database
        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeUpdate)
        val testLedger = ledgerList[ledgerList.size - 1]
        assertThat(testLedger.identifier).isEqualTo(UPDATED_IDENTIFIER)
        assertThat(testLedger.name).isEqualTo(UPDATED_NAME)
        assertThat(testLedger.type).isEqualTo(UPDATED_TYPE)
        assertThat(testLedger.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testLedger.totalValue).isEqualTo(UPDATED_TOTAL_VALUE)
        assertThat(testLedger.showAccountsInChart).isEqualTo(UPDATED_SHOW_ACCOUNTS_IN_CHART)
    }

    @Test
    @Transactional
    fun updateNonExistingLedger() {
        val databaseSizeBeforeUpdate = ledgerRepository.findAll().size

        // Create the Ledger
        val ledgerDTO = ledgerMapper.toDto(ledger)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLedgerMockMvc.perform(
            put("/api/ledgers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Ledger in the database
        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteLedger() {
        // Initialize the database
        ledgerRepository.saveAndFlush(ledger)

        val databaseSizeBeforeDelete = ledgerRepository.findAll().size

        // Delete the ledger
        restLedgerMockMvc.perform(
            delete("/api/ledgers/{id}", ledger.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val ledgerList = ledgerRepository.findAll()
        assertThat(ledgerList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_IDENTIFIER = "AAAAAAAAAA"
        private const val UPDATED_IDENTIFIER = "BBBBBBBBBB"

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private val DEFAULT_TYPE: LedgerType = LedgerType.ASSET
        private val UPDATED_TYPE: LedgerType = LedgerType.LIABILITY

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private val DEFAULT_TOTAL_VALUE: BigDecimal = BigDecimal(1)
        private val UPDATED_TOTAL_VALUE: BigDecimal = BigDecimal(2)

        private const val DEFAULT_SHOW_ACCOUNTS_IN_CHART: Boolean = false
        private const val UPDATED_SHOW_ACCOUNTS_IN_CHART: Boolean = true

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): LedgerView {
            val ledger = LedgerView(
                    identifier = DEFAULT_IDENTIFIER,
                    name = DEFAULT_NAME,
                    type = DEFAULT_TYPE,
                    description = DEFAULT_DESCRIPTION,
                    totalValue = DEFAULT_TOTAL_VALUE,
                    showAccountsInChart = DEFAULT_SHOW_ACCOUNTS_IN_CHART
            )

            return ledger
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): LedgerView {
            val ledger = LedgerView(
                    identifier = UPDATED_IDENTIFIER,
                    name = UPDATED_NAME,
                    type = UPDATED_TYPE,
                    description = UPDATED_DESCRIPTION,
                    totalValue = UPDATED_TOTAL_VALUE,
                    showAccountsInChart = UPDATED_SHOW_ACCOUNTS_IN_CHART
            )

            return ledger
        }
    }
}
