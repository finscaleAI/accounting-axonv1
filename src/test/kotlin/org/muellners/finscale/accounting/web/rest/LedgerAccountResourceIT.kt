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
import org.muellners.finscale.accounting.domain.enumeration.AccountState
import org.muellners.finscale.accounting.domain.enumeration.LedgerType
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView
import org.muellners.finscale.accounting.repository.LedgerAccountViewRepository
import org.muellners.finscale.accounting.service.LedgerAccountService
import org.muellners.finscale.accounting.service.mapper.LedgerAccountMapper
import org.muellners.finscale.accounting.web.rest.errors.ExceptionTranslator
import org.muellners.finscale.accounting.web.rest.witherror.LedgerAccountResource
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
 * Integration tests for the [LedgerAccountResource] REST controller.
 *
 * @see LedgerAccountResource
 */
@SpringBootTest(classes = [SecurityBeanOverrideConfiguration::class, AccountingApp::class])
@AutoConfigureMockMvc
@WithMockUser
class LedgerAccountResourceIT {

    @Autowired
    private lateinit var ledgerAccountRepository: LedgerAccountViewRepository

    @Autowired
    private lateinit var ledgerAccountMapper: LedgerAccountMapper

    @Autowired
    private lateinit var ledgerAccountService: LedgerAccountService

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

    private lateinit var restLedgerAccountMockMvc: MockMvc

    private lateinit var ledgerAccount: LedgerAccountView

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val ledgerAccountResource = LedgerAccountResource(ledgerAccountService)
         this.restLedgerAccountMockMvc = MockMvcBuilders.standaloneSetup(ledgerAccountResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        ledgerAccount = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createLedgerAccount() {
        val databaseSizeBeforeCreate = ledgerAccountRepository.findAll().size

        // Create the LedgerAccount
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)
        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isCreated)

        // Validate the LedgerAccount in the database
        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeCreate + 1)
        val testLedgerAccount = ledgerAccountList[ledgerAccountList.size - 1]
        assertThat(testLedgerAccount.identifier).isEqualTo(DEFAULT_IDENTIFIER)
        assertThat(testLedgerAccount.alternativeAccountNumber).isEqualTo(DEFAULT_ALTERNATIVE_ACCOUNT_NUMBER)
        assertThat(testLedgerAccount.name).isEqualTo(DEFAULT_NAME)
        assertThat(testLedgerAccount.type).isEqualTo(DEFAULT_TYPE)
        assertThat(testLedgerAccount.holders).isEqualTo(DEFAULT_HOLDERS)
        assertThat(testLedgerAccount.signatureAuthorities).isEqualTo(DEFAULT_SIGNATURE_AUTHORITIES)
        assertThat(testLedgerAccount.state).isEqualTo(DEFAULT_STATE)
        assertThat(testLedgerAccount.balance).isEqualTo(DEFAULT_BALANCE)
    }

    @Test
    @Transactional
    fun createLedgerAccountWithExistingId() {
        val databaseSizeBeforeCreate = ledgerAccountRepository.findAll().size

        // Create the LedgerAccount with an existing ID
        ledgerAccount.id = 1L
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        // An entity with an existing ID cannot be created, so this API call must fail
        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        // Validate the LedgerAccount in the database
        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkIdentifierIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountRepository.findAll().size
        // set the field null
        ledgerAccount.identifier = null

        // Create the LedgerAccount, which fails.
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountRepository.findAll().size
        // set the field null
        ledgerAccount.name = null

        // Create the LedgerAccount, which fails.
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkTypeIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountRepository.findAll().size
        // set the field null
        ledgerAccount.type = null

        // Create the LedgerAccount, which fails.
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkHoldersIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountRepository.findAll().size
        // set the field null
        ledgerAccount.holders = null

        // Create the LedgerAccount, which fails.
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkSignatureAuthoritiesIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountRepository.findAll().size
        // set the field null
        ledgerAccount.signatureAuthorities = null

        // Create the LedgerAccount, which fails.
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkStateIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountRepository.findAll().size
        // set the field null
        ledgerAccount.state = null

        // Create the LedgerAccount, which fails.
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        restLedgerAccountMockMvc.perform(
            post("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllLedgerAccounts() {
        // Initialize the database
        ledgerAccountRepository.saveAndFlush(ledgerAccount)

        // Get all the ledgerAccountList
        restLedgerAccountMockMvc.perform(get("/api/ledger-accounts?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ledgerAccount.id?.toInt())))
            .andExpect(jsonPath("$.[*].identifier").value(hasItem(DEFAULT_IDENTIFIER)))
            .andExpect(jsonPath("$.[*].alternativeAccountNumber").value(hasItem(DEFAULT_ALTERNATIVE_ACCOUNT_NUMBER)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].holders").value(hasItem(DEFAULT_HOLDERS)))
            .andExpect(jsonPath("$.[*].signatureAuthorities").value(hasItem(DEFAULT_SIGNATURE_AUTHORITIES)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE?.toInt()))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getLedgerAccount() {
        // Initialize the database
        ledgerAccountRepository.saveAndFlush(ledgerAccount)

        val id = ledgerAccount.id
        assertNotNull(id)

        // Get the ledgerAccount
        restLedgerAccountMockMvc.perform(get("/api/ledger-accounts/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ledgerAccount.id?.toInt()))
            .andExpect(jsonPath("$.identifier").value(DEFAULT_IDENTIFIER))
            .andExpect(jsonPath("$.alternativeAccountNumber").value(DEFAULT_ALTERNATIVE_ACCOUNT_NUMBER))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.holders").value(DEFAULT_HOLDERS))
            .andExpect(jsonPath("$.signatureAuthorities").value(DEFAULT_SIGNATURE_AUTHORITIES))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()))
            .andExpect(jsonPath("$.balance").value(DEFAULT_BALANCE?.toInt())) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingLedgerAccount() {
        // Get the ledgerAccount
        restLedgerAccountMockMvc.perform(get("/api/ledger-accounts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateLedgerAccount() {
        // Initialize the database
        ledgerAccountRepository.saveAndFlush(ledgerAccount)

        val databaseSizeBeforeUpdate = ledgerAccountRepository.findAll().size

        // Update the ledgerAccount
        val id = ledgerAccount.id
        assertNotNull(id)
        val updatedLedgerAccount = ledgerAccountRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedLedgerAccount are not directly saved in db
        em.detach(updatedLedgerAccount)
        updatedLedgerAccount.identifier = UPDATED_IDENTIFIER
        updatedLedgerAccount.alternativeAccountNumber = UPDATED_ALTERNATIVE_ACCOUNT_NUMBER
        updatedLedgerAccount.name = UPDATED_NAME
        updatedLedgerAccount.type = UPDATED_TYPE
        updatedLedgerAccount.holders = UPDATED_HOLDERS
        updatedLedgerAccount.signatureAuthorities = UPDATED_SIGNATURE_AUTHORITIES
        updatedLedgerAccount.state = UPDATED_STATE
        updatedLedgerAccount.balance = UPDATED_BALANCE
        val ledgerAccountDTO = ledgerAccountMapper.toDto(updatedLedgerAccount)

        restLedgerAccountMockMvc.perform(
            put("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isOk)

        // Validate the LedgerAccount in the database
        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeUpdate)
        val testLedgerAccount = ledgerAccountList[ledgerAccountList.size - 1]
        assertThat(testLedgerAccount.identifier).isEqualTo(UPDATED_IDENTIFIER)
        assertThat(testLedgerAccount.alternativeAccountNumber).isEqualTo(UPDATED_ALTERNATIVE_ACCOUNT_NUMBER)
        assertThat(testLedgerAccount.name).isEqualTo(UPDATED_NAME)
        assertThat(testLedgerAccount.type).isEqualTo(UPDATED_TYPE)
        assertThat(testLedgerAccount.holders).isEqualTo(UPDATED_HOLDERS)
        assertThat(testLedgerAccount.signatureAuthorities).isEqualTo(UPDATED_SIGNATURE_AUTHORITIES)
        assertThat(testLedgerAccount.state).isEqualTo(UPDATED_STATE)
        assertThat(testLedgerAccount.balance).isEqualTo(UPDATED_BALANCE)
    }

    @Test
    @Transactional
    fun updateNonExistingLedgerAccount() {
        val databaseSizeBeforeUpdate = ledgerAccountRepository.findAll().size

        // Create the LedgerAccount
        val ledgerAccountDTO = ledgerAccountMapper.toDto(ledgerAccount)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLedgerAccountMockMvc.perform(
            put("/api/ledger-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountDTO))
        ).andExpect(status().isBadRequest)

        // Validate the LedgerAccount in the database
        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteLedgerAccount() {
        // Initialize the database
        ledgerAccountRepository.saveAndFlush(ledgerAccount)

        val databaseSizeBeforeDelete = ledgerAccountRepository.findAll().size

        // Delete the ledgerAccount
        restLedgerAccountMockMvc.perform(
            delete("/api/ledger-accounts/{id}", ledgerAccount.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val ledgerAccountList = ledgerAccountRepository.findAll()
        assertThat(ledgerAccountList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_IDENTIFIER = "AAAAAAAAAA"
        private const val UPDATED_IDENTIFIER = "BBBBBBBBBB"

        private const val DEFAULT_ALTERNATIVE_ACCOUNT_NUMBER = "AAAAAAAAAA"
        private const val UPDATED_ALTERNATIVE_ACCOUNT_NUMBER = "BBBBBBBBBB"

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private val DEFAULT_TYPE: LedgerType = LedgerType.ASSET
        private val UPDATED_TYPE: LedgerType = LedgerType.LIABILITY

        private const val DEFAULT_HOLDERS = "AAAAAAAAAA"
        private const val UPDATED_HOLDERS = "BBBBBBBBBB"

        private const val DEFAULT_SIGNATURE_AUTHORITIES = "AAAAAAAAAA"
        private const val UPDATED_SIGNATURE_AUTHORITIES = "BBBBBBBBBB"

        private val DEFAULT_STATE: AccountState = AccountState.OPEN
        private val UPDATED_STATE: AccountState = AccountState.LOCKED

        private val DEFAULT_BALANCE: BigDecimal = BigDecimal(1)
        private val UPDATED_BALANCE: BigDecimal = BigDecimal(2)

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): LedgerAccountView {
            val ledgerAccount = LedgerAccountView(
                    identifier = DEFAULT_IDENTIFIER,
                    alternativeAccountNumber = DEFAULT_ALTERNATIVE_ACCOUNT_NUMBER,
                    name = DEFAULT_NAME,
                    type = DEFAULT_TYPE,
                    holders = DEFAULT_HOLDERS,
                    signatureAuthorities = DEFAULT_SIGNATURE_AUTHORITIES,
                    state = DEFAULT_STATE,
                    balance = DEFAULT_BALANCE
            )

            return ledgerAccount
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): LedgerAccountView {
            val ledgerAccount = LedgerAccountView(
                    identifier = UPDATED_IDENTIFIER,
                    alternativeAccountNumber = UPDATED_ALTERNATIVE_ACCOUNT_NUMBER,
                    name = UPDATED_NAME,
                    type = UPDATED_TYPE,
                    holders = UPDATED_HOLDERS,
                    signatureAuthorities = UPDATED_SIGNATURE_AUTHORITIES,
                    state = UPDATED_STATE,
                    balance = UPDATED_BALANCE
            )

            return ledgerAccount
        }
    }
}
