package org.muellners.finscale.accounting.web.rest

import io.github.jhipster.web.util.HeaderUtil
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.validation.Valid
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.muellners.finscale.accounting.domain.ledger.commands.CreateLedgerCommand
import org.muellners.finscale.accounting.domain.ledger.commands.DeleteLedgerCommand
import org.muellners.finscale.accounting.domain.ledger.queries.FindAllLedgerQuery
import org.muellners.finscale.accounting.domain.ledger.queries.FindLedgerQuery
import org.muellners.finscale.accounting.domain.ledger.queries.LedgerIdentifierExistsQuery
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.muellners.finscale.accounting.service.dto.LedgerDTO
import org.muellners.finscale.accounting.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val ENTITY_NAME = "accountingLedgerView"
/**
 * REST controller for managing [org.muellners.finscale.accounting.domain.LedgerView].
 */
@RestController
@RequestMapping("/unsecured/ledgers")
class LedgerViewResource(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /ledger-views` : Create a new ledgerView.
     *
     * @param ledgerDTO the ledgerViewDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new ledgerViewDTO, or with status `400 (Bad Request)` if the ledgerView has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping
    fun createLedgerView(@Valid @RequestBody ledgerDTO: LedgerDTO): CompletableFuture<LedgerDTO> {
        log.debug("REST request to save LedgerView : $ledgerDTO")
        if (ledgerDTO.id != null) {
            throw BadRequestAlertException(
                "A new ledgerView cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }

        if (ledgerDTO.identifier != null) {
            val identifierExistsFuture = queryGateway.query(
                LedgerIdentifierExistsQuery(ledgerDTO.identifier!!),
                ResponseTypes.instanceOf(Boolean::class.java))

            if (identifierExistsFuture.get()) {
                throw BadRequestAlertException(
                    "A new ledgerView with the same identifier exsists",
                    ENTITY_NAME, "identifierexists"
                )
            }
        }

        return commandGateway.send(
            CreateLedgerCommand(
                id = UUID.randomUUID(),
                identifier = ledgerDTO.identifier,
                name = ledgerDTO.name,
                type = ledgerDTO.type,
                description = ledgerDTO.description,
                totalValue = ledgerDTO.totalValue,
                showAccountsInChart = ledgerDTO.showAccountsInChart,
                parentLedgerId = ledgerDTO.parentLedgerId
            )
        )
    }

//    /**
//     * `PUT  /ledger-views` : Updates an existing ledgerView.
//     *
//     * @param ledgerDTO the ledgerViewDTO to update.
//     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated ledgerViewDTO,
//     * or with status `400 (Bad Request)` if the ledgerViewDTO is not valid,
//     * or with status `500 (Internal Server Error)` if the ledgerViewDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PutMapping("/ledger-views")
//    fun updateLedgerView(@Valid @RequestBody ledgerDTO: LedgerDTO): ResponseEntity<LedgerDTO> {
//        log.debug("REST request to update LedgerView : $ledgerDTO")
//        if (ledgerDTO.id == null) {
//            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
//        }
//        val result = ledgerViewService.save(ledgerDTO)
//        return ResponseEntity.ok()
//            .headers(
//                HeaderUtil.createEntityUpdateAlert(
//                    applicationName, true, ENTITY_NAME,
//                     ledgerDTO.id.toString()
//                )
//            )
//            .body(result)
//    }

    /**
     * `GET  /ledger-views` : get all the ledgerViews.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of ledgerViews in body.
     */
    @GetMapping
    fun getAllLedgerViews(): CompletableFuture<MutableList<LedgerView>> {
        log.debug("REST request to get all LedgerViews")

        return queryGateway.query(FindAllLedgerQuery(), ResponseTypes.multipleInstancesOf(LedgerView::class.java))
    }

    /**
     * `GET  /ledgers/:id` : get the "id" ledgerView.
     *
     * @param id the id of the ledgerViewDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the ledgerViewDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/{id}")
    fun getLedgerView(@PathVariable id: String): CompletableFuture<LedgerView> {
        log.debug("REST request to get LedgerView : $id")
        return queryGateway.query(FindLedgerQuery(id), ResponseTypes.instanceOf(LedgerView::class.java))
    }

    /**
     *  `DELETE  /ledger-views/:id` : delete the "id" ledgerView.
     *
     * @param id the id of the ledgerViewDTO to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/{id}")
    fun deleteLedgerView(@PathVariable id: String): ResponseEntity<Void> {
        log.debug("REST request to delete LedgerView : $id")

        commandGateway.send<String>(DeleteLedgerCommand(UUID.fromString(id)))

        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
