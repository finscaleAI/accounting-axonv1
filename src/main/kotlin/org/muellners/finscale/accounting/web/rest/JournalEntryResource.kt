package org.muellners.finscale.accounting.web.rest

import java.math.BigDecimal
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.validation.Valid
import kotlin.collections.HashMap
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.muellners.finscale.accounting.domain.accounting.api.BookJournalEntryCommand
import org.muellners.finscale.accounting.domain.journal.api.PostJournalEntryCommand
import org.muellners.finscale.accounting.domain.journal.queries.FindJournalEntryQuery
import org.muellners.finscale.accounting.domain.journal.views.JournalEntryView
import org.muellners.finscale.accounting.service.dto.*
import org.muellners.finscale.accounting.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val ENTITY_NAME = "accountingJournalEntry"
/**
 * REST controller for managing [org.muellners.finscale.accounting.domain.journal.views.JournalEntryView].
 */
@RestController
@RequestMapping("/unsecured/journalEntries")
class JournalEntryResource(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /journalsEntries` : Create a new journal entry.
     *
     * @param journalEntryDTO the journalEntryDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new journalEntryDTO, or with status `400 (Bad Request)` if the journalEntry has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping
    fun createJournalEntry(@Valid @RequestBody journalEntryDTO: JournalEntryDTO): CompletableFuture<JournalEntryDTO> {
        log.debug("REST request to save JournalEntry : $journalEntryDTO")

        if (journalEntryDTO.id != null) {
            throw BadRequestAlertException(
                "A new journal entry cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }

        val debtors: HashMap<UUID, BigDecimal> = hashMapOf()
        journalEntryDTO.debtors?.forEach {
            debtors[it.accountId!!] = it.amount!!
        }

        val creditors: HashMap<UUID, BigDecimal> = hashMapOf()
        journalEntryDTO.creditors?.forEach {
            debtors[it.accountId!!] = it.amount!!
        }

        return commandGateway.send(
            PostJournalEntryCommand(
                id = UUID.randomUUID(),
                transactionTypeId = journalEntryDTO.transactionTypeId,
                transactionDate = journalEntryDTO.transactionDate,
                debtors = debtors,
                creditors = creditors,
                note = journalEntryDTO.note,
                message = journalEntryDTO.message
            )
        )
    }

//    /**
//     * `GET  /journalEntries` : get all the ledgerAccounts.
//     *
//
//     * @return the [ResponseEntity] with status `200 (OK)` and the list of ledgerAccount in body.
//     */
//    @GetMapping
//    fun getAllJournalEntries(): CompletableFuture<MutableList<JournalEntryView>> {
//        log.debug("REST request to get all JournalEntries")
//
//        return queryGateway.query(
//            FindAllJournalEntries(),
//            ResponseTypes.multipleInstancesOf(JournalEntryView::class.java)
//        )
//    }

    /**
     * `GET  /journalEntries/:id` : get the "id" journalEntry.
     *
     * @param id the id of the journalEntryDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the journalEntryDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/{id}")
    fun getJournalEntry(@PathVariable id: UUID): CompletableFuture<JournalEntryView> {
        log.debug("REST request to get JournalEntry : $id")

        return queryGateway.query(
            FindJournalEntryQuery(id = id),
            ResponseTypes.instanceOf(JournalEntryView::class.java)
        )
    }

        /**
     * `POST  /journalEntries/:id/release` : release the "id" journalEntry.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of journalEntry in body.
     */
    @PostMapping("/{id}/release")
    fun releaseJournalEntry(@PathVariable id: UUID): CompletableFuture<MutableList<JournalEntryView>> {
        log.debug("REST request to release JournalEntry : $id")

        return commandGateway.send(
            BookJournalEntryCommand(id = id)
        )
    }
}
