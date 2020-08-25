package org.muellners.finscale.accounting.web.rest

import io.github.jhipster.web.util.HeaderUtil
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.validation.Valid
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.muellners.finscale.accounting.domain.journal.api.CreateTransactionTypeCommand
import org.muellners.finscale.accounting.domain.journal.api.ModifyTransactionTypeCommand
import org.muellners.finscale.accounting.domain.journal.queries.FindAllTransactionTypesQuery
import org.muellners.finscale.accounting.domain.journal.queries.FindTransactionTypeQuery
import org.muellners.finscale.accounting.domain.journal.queries.TransactionTypeIdentifierExistsQuery
import org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView
import org.muellners.finscale.accounting.service.dto.TransactionTypeDTO
import org.muellners.finscale.accounting.service.dto.UpdateTransactionTypeDTO
import org.muellners.finscale.accounting.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val ENTITY_NAME = "accountingTransactionType"
/**
 * REST controller for managing [org.muellners.finscale.accounting.domain.journal.views.TransactionTypeView].
 */
@RestController
@RequestMapping("/unsecured/transactionTypes")
class TransactionTypeResource(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /transactionTypes` : Create a new transactionType.
     *
     * @param transactionTypeDTO the transactionTypeDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new transactionTypeDTO, or with status `400 (Bad Request)` if the transactionType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping
    fun createTransactionType(@Valid @RequestBody transactionTypeDTO: TransactionTypeDTO): CompletableFuture<TransactionTypeDTO> {
        log.debug("REST request to save TransactionType : $transactionTypeDTO")
        if (transactionTypeDTO.id != null) {
            throw BadRequestAlertException(
                "A new transaction type cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }

        if (transactionTypeDTO.identifier != null) {
            val identifierExists = queryGateway.query(
                TransactionTypeIdentifierExistsQuery(transactionTypeDTO.identifier!!),
                ResponseTypes.instanceOf(Boolean::class.java)).get()

            if (identifierExists) {
                throw BadRequestAlertException(
                    "A new transaction type with the same identifier exists",
                    ENTITY_NAME, "identifierexists"
                )
            }
        }

        return commandGateway.send(
            CreateTransactionTypeCommand(
                id = UUID.randomUUID(),
                identifier = transactionTypeDTO.identifier,
                name = transactionTypeDTO.name,
                description = transactionTypeDTO.description
            )
        )
    }

    /**
     * `PUT  /transactionTypes` : Updates an existing transactionType.
     *
     * @param updateTransactionTypeDTO the transactionTypeDTO to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated transactionTypeDTO,
     * or with status `400 (Bad Request)` if the transactionTypeDTO is not valid,
     * or with status `500 (Internal Server Error)` if the transactionTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    fun updateTransactionType(
        @PathVariable id: UUID,
        @Valid @RequestBody updateTransactionTypeDTO: UpdateTransactionTypeDTO
    ): ResponseEntity<Void> {
        log.debug("REST request to update TransactionType : $updateTransactionTypeDTO")

        commandGateway.send<TransactionTypeDTO>(
            ModifyTransactionTypeCommand(
                id = id,
                name = updateTransactionTypeDTO.name,
                description = updateTransactionTypeDTO.description
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
     * `GET  /transactionTypes` : get all the transactionTypes.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of transactionTypes in body.
     */
    @GetMapping
    fun getAllTransactionTypes(): CompletableFuture<MutableList<TransactionTypeView>> {
        log.debug("REST request to get all transactionTypes")

        return queryGateway.query(
            FindAllTransactionTypesQuery(),
            ResponseTypes.multipleInstancesOf(TransactionTypeView::class.java)
        )
    }

    /**
     * `GET  /transactionTypes/:id` : get the "id" transactionType.
     *
     * @param id the id of the transactionTypeDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the transactionTypeDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/{id}")
    fun getTransactionType(@PathVariable id: UUID): CompletableFuture<TransactionTypeView> {
        log.debug("REST request to get TransactionType : $id")

        return queryGateway.query(
            FindTransactionTypeQuery(id = id),
            ResponseTypes.instanceOf(TransactionTypeView::class.java)
        )
    }

//    /**
//     *  `DELETE  /transactionTypes/:id` : delete the "id" transactionType.
//     *
//     * @param id the id of the transactionTypeDTO to delete.
//     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
//     */
//    @DeleteMapping("/{id}")
//    fun deleteTransactionType(@PathVariable id: UUID): ResponseEntity<Void> {
//        log.debug("REST request to delete TransactionType : $id")
//
//        commandGateway.send<String>(DeleteTransactionTypeCommand(id))
//
//        return ResponseEntity.noContent()
//            .headers(
//                HeaderUtil.createEntityDeletionAlert(
//                    applicationName, true, ENTITY_NAME, id.toString())
//            ).build()
//    }
}
