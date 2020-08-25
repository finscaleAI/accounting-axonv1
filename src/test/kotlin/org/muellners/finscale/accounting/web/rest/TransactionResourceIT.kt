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
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.journal.views.TransactionView
import org.muellners.finscale.accounting.repository.TransactionViewRepository
import org.muellners.finscale.accounting.service.TransactionService
import org.muellners.finscale.accounting.service.mapper.TransactionMapper
import org.muellners.finscale.accounting.web.rest.errors.ExceptionTranslator
import org.muellners.finscale.accounting.web.rest.witherror.TransactionResource
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
 * Integration tests for the [TransactionResource] REST controller.
 *
 * @see TransactionResource
 */
@SpringBootTest(classes = [SecurityBeanOverrideConfiguration::class, AccountingApp::class])
@AutoConfigureMockMvc
@WithMockUser
class TransactionResourceIT {

    @Autowired
    private lateinit var transactionRepository: TransactionViewRepository

    @Autowired
    private lateinit var transactionMapper: TransactionMapper

    @Autowired
    private lateinit var transactionService: TransactionService

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

    private lateinit var restTransactionMockMvc: MockMvc

    private lateinit var transaction: TransactionView

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val transactionResource = TransactionResource(transactionService)
         this.restTransactionMockMvc = MockMvcBuilders.standaloneSetup(transactionResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        transaction = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createTransaction() {
        val databaseSizeBeforeCreate = transactionRepository.findAll().size

        // Create the Transaction
        val transactionDTO = transactionMapper.toDto(transaction)
        restTransactionMockMvc.perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionDTO))
        ).andExpect(status().isCreated)

        // Validate the Transaction in the database
        val transactionList = transactionRepository.findAll()
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1)
        val testTransaction = transactionList[transactionList.size - 1]
        assertThat(testTransaction.transactionSide).isEqualTo(DEFAULT_TRANSACTION_SIDE)
        assertThat(testTransaction.amount).isEqualTo(DEFAULT_AMOUNT)
    }

    @Test
    @Transactional
    fun createTransactionWithExistingId() {
        val databaseSizeBeforeCreate = transactionRepository.findAll().size

        // Create the Transaction with an existing ID
        transaction.id = 1L
        val transactionDTO = transactionMapper.toDto(transaction)

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransactionMockMvc.perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Transaction in the database
        val transactionList = transactionRepository.findAll()
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkTransactionSideIsRequired() {
        val databaseSizeBeforeTest = transactionRepository.findAll().size
        // set the field null
        transaction.transactionSide = null

        // Create the Transaction, which fails.
        val transactionDTO = transactionMapper.toDto(transaction)

        restTransactionMockMvc.perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionDTO))
        ).andExpect(status().isBadRequest)

        val transactionList = transactionRepository.findAll()
        assertThat(transactionList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkAmountIsRequired() {
        val databaseSizeBeforeTest = transactionRepository.findAll().size
        // set the field null
        transaction.amount = null

        // Create the Transaction, which fails.
        val transactionDTO = transactionMapper.toDto(transaction)

        restTransactionMockMvc.perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionDTO))
        ).andExpect(status().isBadRequest)

        val transactionList = transactionRepository.findAll()
        assertThat(transactionList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllTransactions() {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction)

        // Get all the transactionList
        restTransactionMockMvc.perform(get("/api/transactions?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.id?.toInt())))
            .andExpect(jsonPath("$.[*].transactionSide").value(hasItem(DEFAULT_TRANSACTION_SIDE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT?.toInt()))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getTransaction() {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction)

        val id = transaction.id
        assertNotNull(id)

        // Get the transaction
        restTransactionMockMvc.perform(get("/api/transactions/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transaction.id?.toInt()))
            .andExpect(jsonPath("$.transactionSide").value(DEFAULT_TRANSACTION_SIDE.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT?.toInt())) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingTransaction() {
        // Get the transaction
        restTransactionMockMvc.perform(get("/api/transactions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateTransaction() {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction)

        val databaseSizeBeforeUpdate = transactionRepository.findAll().size

        // Update the transaction
        val id = transaction.id
        assertNotNull(id)
        val updatedTransaction = transactionRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedTransaction are not directly saved in db
        em.detach(updatedTransaction)
        updatedTransaction.transactionSide = UPDATED_TRANSACTION_SIDE
        updatedTransaction.amount = UPDATED_AMOUNT
        val transactionDTO = transactionMapper.toDto(updatedTransaction)

        restTransactionMockMvc.perform(
            put("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionDTO))
        ).andExpect(status().isOk)

        // Validate the Transaction in the database
        val transactionList = transactionRepository.findAll()
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate)
        val testTransaction = transactionList[transactionList.size - 1]
        assertThat(testTransaction.transactionSide).isEqualTo(UPDATED_TRANSACTION_SIDE)
        assertThat(testTransaction.amount).isEqualTo(UPDATED_AMOUNT)
    }

    @Test
    @Transactional
    fun updateNonExistingTransaction() {
        val databaseSizeBeforeUpdate = transactionRepository.findAll().size

        // Create the Transaction
        val transactionDTO = transactionMapper.toDto(transaction)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc.perform(
            put("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Transaction in the database
        val transactionList = transactionRepository.findAll()
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteTransaction() {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction)

        val databaseSizeBeforeDelete = transactionRepository.findAll().size

        // Delete the transaction
        restTransactionMockMvc.perform(
            delete("/api/transactions/{id}", transaction.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val transactionList = transactionRepository.findAll()
        assertThat(transactionList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private val DEFAULT_TRANSACTION_SIDE: TransactionSide = TransactionSide.DEBIT
        private val UPDATED_TRANSACTION_SIDE: TransactionSide = TransactionSide.CREDIT

        private val DEFAULT_AMOUNT: BigDecimal = BigDecimal(1)
        private val UPDATED_AMOUNT: BigDecimal = BigDecimal(2)

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): TransactionView {
            val transaction = TransactionView(
                    transactionSide = DEFAULT_TRANSACTION_SIDE,
                    amount = DEFAULT_AMOUNT
            )

            return transaction
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): TransactionView {
            val transaction = TransactionView(
                    transactionSide = UPDATED_TRANSACTION_SIDE,
                    amount = UPDATED_AMOUNT
            )

            return transaction
        }
    }
}
