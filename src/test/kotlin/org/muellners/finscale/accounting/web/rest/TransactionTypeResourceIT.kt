package org.muellners.finscale.accounting.web.rest

import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.muellners.finscale.accounting.AccountingApp
import org.muellners.finscale.accounting.config.SecurityBeanOverrideConfiguration
import org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView
import org.muellners.finscale.accounting.repository.TransactionTypeViewRepository
import org.muellners.finscale.accounting.service.TransactionTypeService
import org.muellners.finscale.accounting.service.mapper.TransactionTypeMapper
import org.muellners.finscale.accounting.web.rest.errors.ExceptionTranslator
import org.muellners.finscale.accounting.web.rest.witherror.TransactionTypeResource
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
 * Integration tests for the [TransactionTypeResource] REST controller.
 *
 * @see TransactionTypeResource
 */
@SpringBootTest(classes = [SecurityBeanOverrideConfiguration::class, AccountingApp::class])
@AutoConfigureMockMvc
@WithMockUser
class TransactionTypeResourceIT {

    @Autowired
    private lateinit var transactionTypeRepository: TransactionTypeViewRepository

    @Autowired
    private lateinit var transactionTypeMapper: TransactionTypeMapper

    @Autowired
    private lateinit var transactionTypeService: TransactionTypeService

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

    private lateinit var restTransactionTypeMockMvc: MockMvc

    private lateinit var transactionType: TransactionTypeView

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val transactionTypeResource = TransactionTypeResource(transactionTypeService)
         this.restTransactionTypeMockMvc = MockMvcBuilders.standaloneSetup(transactionTypeResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        transactionType = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createTransactionType() {
        val databaseSizeBeforeCreate = transactionTypeRepository.findAll().size

        // Create the TransactionType
        val transactionTypeDTO = transactionTypeMapper.toDto(transactionType)
        restTransactionTypeMockMvc.perform(
            post("/api/transaction-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionTypeDTO))
        ).andExpect(status().isCreated)

        // Validate the TransactionType in the database
        val transactionTypeList = transactionTypeRepository.findAll()
        assertThat(transactionTypeList).hasSize(databaseSizeBeforeCreate + 1)
        val testTransactionType = transactionTypeList[transactionTypeList.size - 1]
        assertThat(testTransactionType.identifier).isEqualTo(DEFAULT_IDENTIFIER)
        assertThat(testTransactionType.name).isEqualTo(DEFAULT_NAME)
        assertThat(testTransactionType.description).isEqualTo(DEFAULT_DESCRIPTION)
    }

    @Test
    @Transactional
    fun createTransactionTypeWithExistingId() {
        val databaseSizeBeforeCreate = transactionTypeRepository.findAll().size

        // Create the TransactionType with an existing ID
        transactionType.id = 1L
        val transactionTypeDTO = transactionTypeMapper.toDto(transactionType)

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransactionTypeMockMvc.perform(
            post("/api/transaction-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionTypeDTO))
        ).andExpect(status().isBadRequest)

        // Validate the TransactionType in the database
        val transactionTypeList = transactionTypeRepository.findAll()
        assertThat(transactionTypeList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkIdentifierIsRequired() {
        val databaseSizeBeforeTest = transactionTypeRepository.findAll().size
        // set the field null
        transactionType.identifier = null

        // Create the TransactionType, which fails.
        val transactionTypeDTO = transactionTypeMapper.toDto(transactionType)

        restTransactionTypeMockMvc.perform(
            post("/api/transaction-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionTypeDTO))
        ).andExpect(status().isBadRequest)

        val transactionTypeList = transactionTypeRepository.findAll()
        assertThat(transactionTypeList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = transactionTypeRepository.findAll().size
        // set the field null
        transactionType.name = null

        // Create the TransactionType, which fails.
        val transactionTypeDTO = transactionTypeMapper.toDto(transactionType)

        restTransactionTypeMockMvc.perform(
            post("/api/transaction-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionTypeDTO))
        ).andExpect(status().isBadRequest)

        val transactionTypeList = transactionTypeRepository.findAll()
        assertThat(transactionTypeList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllTransactionTypes() {
        // Initialize the database
        transactionTypeRepository.saveAndFlush(transactionType)

        // Get all the transactionTypeList
        restTransactionTypeMockMvc.perform(get("/api/transaction-types?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transactionType.id?.toInt())))
            .andExpect(jsonPath("$.[*].identifier").value(hasItem(DEFAULT_IDENTIFIER)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getTransactionType() {
        // Initialize the database
        transactionTypeRepository.saveAndFlush(transactionType)

        val id = transactionType.id
        assertNotNull(id)

        // Get the transactionType
        restTransactionTypeMockMvc.perform(get("/api/transaction-types/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transactionType.id?.toInt()))
            .andExpect(jsonPath("$.identifier").value(DEFAULT_IDENTIFIER))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION)) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingTransactionType() {
        // Get the transactionType
        restTransactionTypeMockMvc.perform(get("/api/transaction-types/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateTransactionType() {
        // Initialize the database
        transactionTypeRepository.saveAndFlush(transactionType)

        val databaseSizeBeforeUpdate = transactionTypeRepository.findAll().size

        // Update the transactionType
        val id = transactionType.id
        assertNotNull(id)
        val updatedTransactionType = transactionTypeRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedTransactionType are not directly saved in db
        em.detach(updatedTransactionType)
        updatedTransactionType.identifier = UPDATED_IDENTIFIER
        updatedTransactionType.name = UPDATED_NAME
        updatedTransactionType.description = UPDATED_DESCRIPTION
        val transactionTypeDTO = transactionTypeMapper.toDto(updatedTransactionType)

        restTransactionTypeMockMvc.perform(
            put("/api/transaction-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionTypeDTO))
        ).andExpect(status().isOk)

        // Validate the TransactionType in the database
        val transactionTypeList = transactionTypeRepository.findAll()
        assertThat(transactionTypeList).hasSize(databaseSizeBeforeUpdate)
        val testTransactionType = transactionTypeList[transactionTypeList.size - 1]
        assertThat(testTransactionType.identifier).isEqualTo(UPDATED_IDENTIFIER)
        assertThat(testTransactionType.name).isEqualTo(UPDATED_NAME)
        assertThat(testTransactionType.description).isEqualTo(UPDATED_DESCRIPTION)
    }

    @Test
    @Transactional
    fun updateNonExistingTransactionType() {
        val databaseSizeBeforeUpdate = transactionTypeRepository.findAll().size

        // Create the TransactionType
        val transactionTypeDTO = transactionTypeMapper.toDto(transactionType)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionTypeMockMvc.perform(
            put("/api/transaction-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(transactionTypeDTO))
        ).andExpect(status().isBadRequest)

        // Validate the TransactionType in the database
        val transactionTypeList = transactionTypeRepository.findAll()
        assertThat(transactionTypeList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteTransactionType() {
        // Initialize the database
        transactionTypeRepository.saveAndFlush(transactionType)

        val databaseSizeBeforeDelete = transactionTypeRepository.findAll().size

        // Delete the transactionType
        restTransactionTypeMockMvc.perform(
            delete("/api/transaction-types/{id}", transactionType.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val transactionTypeList = transactionTypeRepository.findAll()
        assertThat(transactionTypeList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_IDENTIFIER = "AAAAAAAAAA"
        private const val UPDATED_IDENTIFIER = "BBBBBBBBBB"

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): TransactionTypeView {
            val transactionType = TransactionTypeView(
                    identifier = DEFAULT_IDENTIFIER,
                    name = DEFAULT_NAME,
                    description = DEFAULT_DESCRIPTION
            )

            return transactionType
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): TransactionTypeView {
            val transactionType = TransactionTypeView(
                    identifier = UPDATED_IDENTIFIER,
                    name = UPDATED_NAME,
                    description = UPDATED_DESCRIPTION
            )

            return transactionType
        }
    }
}
