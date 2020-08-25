package org.muellners.finscale.accounting.web.rest

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.muellners.finscale.accounting.AccountingApp
import org.muellners.finscale.accounting.config.SecurityBeanOverrideConfiguration
import org.muellners.finscale.accounting.domain.enumeration.AccountAction
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountActionView
import org.muellners.finscale.accounting.repository.LedgerAccountActionViewRepository
import org.muellners.finscale.accounting.service.LedgerAccountActionService
import org.muellners.finscale.accounting.service.mapper.LedgerAccountActionMapper
import org.muellners.finscale.accounting.web.rest.errors.ExceptionTranslator
import org.muellners.finscale.accounting.web.rest.witherror.LedgerAccountActionResource
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
 * Integration tests for the [LedgerAccountActionResource] REST controller.
 *
 * @see LedgerAccountActionResource
 */
@SpringBootTest(classes = [SecurityBeanOverrideConfiguration::class, AccountingApp::class])
@AutoConfigureMockMvc
@WithMockUser
class LedgerAccountActionResourceIT {

    @Autowired
    private lateinit var ledgerAccountActionRepository: LedgerAccountActionViewRepository

    @Autowired
    private lateinit var ledgerAccountActionMapper: LedgerAccountActionMapper

    @Autowired
    private lateinit var ledgerAccountActionService: LedgerAccountActionService

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

    private lateinit var restLedgerAccountActionMockMvc: MockMvc

    private lateinit var ledgerAccountAction: LedgerAccountActionView

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val ledgerAccountActionResource = LedgerAccountActionResource(ledgerAccountActionService)
         this.restLedgerAccountActionMockMvc = MockMvcBuilders.standaloneSetup(ledgerAccountActionResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        ledgerAccountAction = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createLedgerAccountAction() {
        val databaseSizeBeforeCreate = ledgerAccountActionRepository.findAll().size

        // Create the LedgerAccountAction
        val ledgerAccountActionDTO = ledgerAccountActionMapper.toDto(ledgerAccountAction)
        restLedgerAccountActionMockMvc.perform(
            post("/api/ledger-account-actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountActionDTO))
        ).andExpect(status().isCreated)

        // Validate the LedgerAccountAction in the database
        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeCreate + 1)
        val testLedgerAccountAction = ledgerAccountActionList[ledgerAccountActionList.size - 1]
        assertThat(testLedgerAccountAction.action).isEqualTo(DEFAULT_ACTION)
        assertThat(testLedgerAccountAction.comment).isEqualTo(DEFAULT_COMMENT)
        assertThat(testLedgerAccountAction.occuredAt).isEqualTo(DEFAULT_OCCURED_AT)
    }

    @Test
    @Transactional
    fun createLedgerAccountActionWithExistingId() {
        val databaseSizeBeforeCreate = ledgerAccountActionRepository.findAll().size

        // Create the LedgerAccountAction with an existing ID
        ledgerAccountAction.id = 1L
        val ledgerAccountActionDTO = ledgerAccountActionMapper.toDto(ledgerAccountAction)

        // An entity with an existing ID cannot be created, so this API call must fail
        restLedgerAccountActionMockMvc.perform(
            post("/api/ledger-account-actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountActionDTO))
        ).andExpect(status().isBadRequest)

        // Validate the LedgerAccountAction in the database
        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkActionIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountActionRepository.findAll().size
        // set the field null
        ledgerAccountAction.action = null

        // Create the LedgerAccountAction, which fails.
        val ledgerAccountActionDTO = ledgerAccountActionMapper.toDto(ledgerAccountAction)

        restLedgerAccountActionMockMvc.perform(
            post("/api/ledger-account-actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountActionDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkCommentIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountActionRepository.findAll().size
        // set the field null
        ledgerAccountAction.comment = null

        // Create the LedgerAccountAction, which fails.
        val ledgerAccountActionDTO = ledgerAccountActionMapper.toDto(ledgerAccountAction)

        restLedgerAccountActionMockMvc.perform(
            post("/api/ledger-account-actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountActionDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun checkOccuredAtIsRequired() {
        val databaseSizeBeforeTest = ledgerAccountActionRepository.findAll().size
        // set the field null
        ledgerAccountAction.occuredAt = null

        // Create the LedgerAccountAction, which fails.
        val ledgerAccountActionDTO = ledgerAccountActionMapper.toDto(ledgerAccountAction)

        restLedgerAccountActionMockMvc.perform(
            post("/api/ledger-account-actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountActionDTO))
        ).andExpect(status().isBadRequest)

        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllLedgerAccountActions() {
        // Initialize the database
        ledgerAccountActionRepository.saveAndFlush(ledgerAccountAction)

        // Get all the ledgerAccountActionList
        restLedgerAccountActionMockMvc.perform(get("/api/ledger-account-actions?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ledgerAccountAction.id?.toInt())))
            .andExpect(jsonPath("$.[*].action").value(hasItem(DEFAULT_ACTION.toString())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
            .andExpect(jsonPath("$.[*].occuredAt").value(hasItem(sameInstant(DEFAULT_OCCURED_AT)))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getLedgerAccountAction() {
        // Initialize the database
        ledgerAccountActionRepository.saveAndFlush(ledgerAccountAction)

        val id = ledgerAccountAction.id
        assertNotNull(id)

        // Get the ledgerAccountAction
        restLedgerAccountActionMockMvc.perform(get("/api/ledger-account-actions/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ledgerAccountAction.id?.toInt()))
            .andExpect(jsonPath("$.action").value(DEFAULT_ACTION.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT))
            .andExpect(jsonPath("$.occuredAt").value(sameInstant(DEFAULT_OCCURED_AT))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingLedgerAccountAction() {
        // Get the ledgerAccountAction
        restLedgerAccountActionMockMvc.perform(get("/api/ledger-account-actions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateLedgerAccountAction() {
        // Initialize the database
        ledgerAccountActionRepository.saveAndFlush(ledgerAccountAction)

        val databaseSizeBeforeUpdate = ledgerAccountActionRepository.findAll().size

        // Update the ledgerAccountAction
        val id = ledgerAccountAction.id
        assertNotNull(id)
        val updatedLedgerAccountAction = ledgerAccountActionRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedLedgerAccountAction are not directly saved in db
        em.detach(updatedLedgerAccountAction)
        updatedLedgerAccountAction.action = UPDATED_ACTION
        updatedLedgerAccountAction.comment = UPDATED_COMMENT
        updatedLedgerAccountAction.occuredAt = UPDATED_OCCURED_AT
        val ledgerAccountActionDTO = ledgerAccountActionMapper.toDto(updatedLedgerAccountAction)

        restLedgerAccountActionMockMvc.perform(
            put("/api/ledger-account-actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountActionDTO))
        ).andExpect(status().isOk)

        // Validate the LedgerAccountAction in the database
        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeUpdate)
        val testLedgerAccountAction = ledgerAccountActionList[ledgerAccountActionList.size - 1]
        assertThat(testLedgerAccountAction.action).isEqualTo(UPDATED_ACTION)
        assertThat(testLedgerAccountAction.comment).isEqualTo(UPDATED_COMMENT)
        assertThat(testLedgerAccountAction.occuredAt).isEqualTo(UPDATED_OCCURED_AT)
    }

    @Test
    @Transactional
    fun updateNonExistingLedgerAccountAction() {
        val databaseSizeBeforeUpdate = ledgerAccountActionRepository.findAll().size

        // Create the LedgerAccountAction
        val ledgerAccountActionDTO = ledgerAccountActionMapper.toDto(ledgerAccountAction)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLedgerAccountActionMockMvc.perform(
            put("/api/ledger-account-actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(ledgerAccountActionDTO))
        ).andExpect(status().isBadRequest)

        // Validate the LedgerAccountAction in the database
        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteLedgerAccountAction() {
        // Initialize the database
        ledgerAccountActionRepository.saveAndFlush(ledgerAccountAction)

        val databaseSizeBeforeDelete = ledgerAccountActionRepository.findAll().size

        // Delete the ledgerAccountAction
        restLedgerAccountActionMockMvc.perform(
            delete("/api/ledger-account-actions/{id}", ledgerAccountAction.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val ledgerAccountActionList = ledgerAccountActionRepository.findAll()
        assertThat(ledgerAccountActionList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private val DEFAULT_ACTION: AccountAction = AccountAction.LOCK
        private val UPDATED_ACTION: AccountAction = AccountAction.UNLOCK

        private const val DEFAULT_COMMENT = "AAAAAAAAAA"
        private const val UPDATED_COMMENT = "BBBBBBBBBB"

        private val DEFAULT_OCCURED_AT: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC)
        private val UPDATED_OCCURED_AT: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0)

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): LedgerAccountActionView {
            val ledgerAccountAction = LedgerAccountActionView(
                    action = DEFAULT_ACTION,
                    comment = DEFAULT_COMMENT,
                    occuredAt = DEFAULT_OCCURED_AT
            )

            return ledgerAccountAction
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): LedgerAccountActionView {
            val ledgerAccountAction = LedgerAccountActionView(
                    action = UPDATED_ACTION,
                    comment = UPDATED_COMMENT,
                    occuredAt = UPDATED_OCCURED_AT
            )

            return ledgerAccountAction
        }
    }
}
