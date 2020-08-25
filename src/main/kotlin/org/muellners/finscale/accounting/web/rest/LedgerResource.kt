package org.muellners.finscale.accounting.web.rest

import io.github.jhipster.web.util.HeaderUtil
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.validation.Valid
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.muellners.finscale.accounting.domain.ledger.api.CreateLedgerCommand
import org.muellners.finscale.accounting.domain.ledger.api.DeleteLedgerCommand
import org.muellners.finscale.accounting.domain.ledger.api.ModifyLedgerCommand
import org.muellners.finscale.accounting.domain.ledger.queries.FindAllLedgersQuery
import org.muellners.finscale.accounting.domain.ledger.queries.FindLedgerQuery
import org.muellners.finscale.accounting.domain.ledger.queries.LedgerIdentifierExistsQuery
import org.muellners.finscale.accounting.domain.ledger.queries.SubLedgersExistQuery
import org.muellners.finscale.accounting.domain.ledger.views.LedgerView
import org.muellners.finscale.accounting.service.dto.LedgerDTO
import org.muellners.finscale.accounting.service.dto.UpdateLedgerDTO
import org.muellners.finscale.accounting.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val ENTITY_NAME = "accountingLedger"
/**
 * REST controller for managing [org.muellners.finscale.accounting.domain.ledger.views.LedgerView].
 */
@RestController
@RequestMapping("/unsecured/ledgers")
class LedgerResource(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /ledgers` : Create a new ledger.
     *
     * @param ledgerDTO the ledgerDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new ledgerDTO, or with status `400 (Bad Request)` if the ledgerView has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping
    fun createLedger(@Valid @RequestBody ledgerDTO: LedgerDTO): CompletableFuture<LedgerDTO> {
        log.debug("REST request to save Ledger : $ledgerDTO")
        if (ledgerDTO.id != null) {
            throw BadRequestAlertException(
                "A new ledger cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }

        if (ledgerDTO.identifier != null) {
            val identifierExists = queryGateway.query(
                LedgerIdentifierExistsQuery(ledgerDTO.identifier!!),
                ResponseTypes.instanceOf(Boolean::class.java)).get()

            if (identifierExists) {
                throw BadRequestAlertException(
                    "A ledger with the same identifier exists",
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

    /**
     * `PUT  /ledgers` : Updates an existing ledger.
     *
     * @param updateLedgerDTO the ledgerDTO to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated ledgerDTO,
     * or with status `400 (Bad Request)` if the ledgerDTO is not valid,
     * or with status `500 (Internal Server Error)` if the ledgerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    fun updateLedger(
        @PathVariable id: UUID,
        @Valid @RequestBody updateLedgerDTO: UpdateLedgerDTO
    ): ResponseEntity<Void> {
        log.debug("REST request to update Ledger : $updateLedgerDTO")

        commandGateway.send<LedgerDTO>(
            ModifyLedgerCommand(
                id = id,
                name = updateLedgerDTO.name,
                description = updateLedgerDTO.description,
                showAccountsInChart = updateLedgerDTO.showAccountsInChart
            )
        )

        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME, id.toString()
                )
            ).build()
    }

    /**
     * `GET  /ledgers` : get all the ledgers.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of ledgers in body.
     */
    @GetMapping
    fun getAllLedgers(): CompletableFuture<MutableList<LedgerView>> {
        log.debug("REST request to get all Ledgers")

        return queryGateway.query(
            FindAllLedgersQuery(),
            ResponseTypes.multipleInstancesOf(LedgerView::class.java)
        )
    }

    /**
     * `GET  /ledgers/:id` : get the "id" ledger.
     *
     * @param id the id of the ledgerDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the ledgerDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/{id}")
    fun getLedger(@PathVariable id: UUID): CompletableFuture<LedgerView> {
        log.debug("REST request to get Ledger : $id")

        return queryGateway.query(
            FindLedgerQuery(id),
            ResponseTypes.instanceOf(LedgerView::class.java)
        )
    }

    /**
     *  `DELETE  /ledgers/:id` : delete the "id" ledger.
     *
     * @param id the id of the ledgerDTO to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/{id}")
    fun deleteLedger(@PathVariable id: UUID): ResponseEntity<Void> {
        log.debug("REST request to delete Ledger : $id")

        val subLedgersExist = queryGateway.query(
            SubLedgersExistQuery(parentLedgerId = id),
            ResponseTypes.instanceOf(Boolean::class.java)).get()

        if (subLedgersExist) {
            throw BadRequestAlertException(
                "The ledger cannot be deleted as it has subledgers",
                ENTITY_NAME, "has subledgers"
            )
        }

        commandGateway.send<String>(DeleteLedgerCommand(id))

        return ResponseEntity.noContent()
            .headers(
                HeaderUtil.createEntityDeletionAlert(
                    applicationName, true, ENTITY_NAME, id.toString())
            ).build()
    }
}
