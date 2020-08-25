package org.muellners.finscale.accounting.web.rest

import io.github.jhipster.web.util.HeaderUtil
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.validation.Valid
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.muellners.finscale.accounting.domain.ledger.api.*
import org.muellners.finscale.accounting.domain.ledger.queries.*
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountActionView
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountEntryView
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView
import org.muellners.finscale.accounting.service.dto.*
import org.muellners.finscale.accounting.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val ENTITY_NAME = "accountingLedgerAccount"
/**
 * REST controller for managing [org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView].
 */
@RestController
@RequestMapping("/unsecured/ledgerAccounts")
class LedgerAccountResource(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /ledgerAccounts` : Create a new ledger account.
     *
     * @param ledgerAccountDTO the ledgerAccountDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new ledgerAccountDTO, or with status `400 (Bad Request)` if the ledgerAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping
    fun createLedgerAccount(@Valid @RequestBody ledgerAccountDTO: LedgerAccountDTO): CompletableFuture<LedgerAccountDTO> {
        log.debug("REST request to save LedgerAccount : $ledgerAccountDTO")
        if (ledgerAccountDTO.id != null) {
            throw BadRequestAlertException(
                "A new ledger account cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }

        return commandGateway.send(
            ValidateAndCreateLedgerAccountCommand(
                id = UUID.randomUUID(),
                ledgerId = ledgerAccountDTO.ledgerId,
                identifier = ledgerAccountDTO.identifier,
                alternativeAccountNumber = ledgerAccountDTO.alternativeAccountNumber,
                name = ledgerAccountDTO.name,
                balance = ledgerAccountDTO.balance,
                holderIds = ledgerAccountDTO.holders,
                signatureAuthorityIds = ledgerAccountDTO.signatureAuthorities,
                referenceAccountId = ledgerAccountDTO.referenceAccountId
            )
        )
    }

//    /**
//     * `PUT  /ledgerAccounts` : Updates an existing ledgerAccount.
//     *
//     * @param updateLedgerAccountDTO the ledgerAccountDTO to update.
//     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated ledgerAccountDTO,
//     * or with status `400 (Bad Request)` if the ledgerAccountDTO is not valid,
//     * or with status `500 (Internal Server Error)` if the ledgerAccountDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PutMapping("/{id}")
//    fun updateLedgerAccount(
//        @PathVariable id: UUID,
//        @Valid @RequestBody updateLedgerAccountDTO: UpdateLedgerAccountDTO
//    ): ResponseEntity<Void> {
//        log.debug("REST request to update LedgerAccount : $updateLedgerAccountDTO")
//
//        commandGateway.send<LedgerDTO>(
//            ModifyLedgerAccountCommand(
//                id = updateLedgerAccountDTO.id,
//                name = updateLedgerAccountDTO.name
//            )
//        )
//
//        return ResponseEntity.ok()
//            .headers(
//                HeaderUtil.createEntityUpdateAlert(
//                    applicationName, true, ENTITY_NAME, id.toString()
//                )
//            ).build()
//    }

    /**
     * `GET  /ledgerAccounts` : get all the ledgerAccounts.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of ledgerAccount in body.
     */
    @GetMapping
    fun getAllLedgerAccounts(): CompletableFuture<MutableList<LedgerAccountView>> {
        log.debug("REST request to get all LedgerAccounts")

        return queryGateway.query(
            FindAllLedgerAccountsQuery(),
            ResponseTypes.multipleInstancesOf(LedgerAccountView::class.java)
        )
    }

    /**
     * `GET  /ledgerAccounts/:id` : get the "id" ledgerAccount.
     *
     * @param id the id of the ledgerAccountDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the ledgerAccountDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/{id}")
    fun getLedgerAccount(@PathVariable id: UUID): CompletableFuture<LedgerAccountView> {
        log.debug("REST request to get LedgerAccount : $id")

        return queryGateway.query(
            FindLedgerAccountQuery(id = id),
            ResponseTypes.instanceOf(LedgerAccountView::class.java)
        )
    }

    /**
     * `PATCH  /ledgerAccounts/:id/lock` : lock the "id" ledgerAccount.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and with body the ledgerAccountDTO, or with status `404 (Not Found)`.
     */
    @PatchMapping("/{id}/lock")
    fun lockLedgerAccount(
        @PathVariable id: UUID,
        @Valid @RequestBody ledgerAccountActionDTO: LedgerAccountActionDTO
    ): ResponseEntity<Void> {
        log.debug("REST request to lock LedgerAccount : $id")

        queryGateway.query(
            LockLedgerAccountCommand(id = id, comment = ledgerAccountActionDTO.comment),
            ResponseTypes.instanceOf(LedgerAccountView::class.java)
        )

        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME, id.toString()
                )
            ).build()
    }

    /**
     * `PATCH  /ledgerAccounts/:id/unlock` : unlock the "id" ledgerAccount.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and with body the ledgerAccountDTO, or with status `404 (Not Found)`.
     */
    @PatchMapping("/{id}/unlock")
    fun unlockLedgerAccount(
        @PathVariable id: UUID,
        @Valid @RequestBody ledgerAccountActionDTO: LedgerAccountActionDTO
    ): ResponseEntity<Void> {
        log.debug("REST request to unlock LedgerAccount : $id")

        queryGateway.query(
            UnlockLedgerAccountCommand(id = id, comment = ledgerAccountActionDTO.comment),
            ResponseTypes.instanceOf(LedgerAccountView::class.java)
        )

        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME, id.toString()
                )
            ).build()
    }

    /**
     * `PATCH  /ledgerAccounts/:id/close` : close the "id" ledgerAccount.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and with body the ledgerAccountDTO, or with status `404 (Not Found)`.
     */
    @PatchMapping("/{id}/close")
    fun closeLedgerAccount(
        @PathVariable id: UUID,
        @Valid @RequestBody ledgerAccountActionDTO: LedgerAccountActionDTO
    ): ResponseEntity<Void> {
        log.debug("REST request to close LedgerAccount : $id")

        queryGateway.query(
            CloseLedgerAccountCommand(id = id, comment = ledgerAccountActionDTO.comment),
            ResponseTypes.instanceOf(LedgerAccountView::class.java)
        )

        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME, id.toString()
                )
            ).build()
    }

    /**
     * `PATCH  /ledgerAccounts/:id/reopen` : close the "id" ledgerAccount.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and with body the ledgerAccountDTO, or with status `404 (Not Found)`.
     */
    @PatchMapping("/{id}/reopen")
    fun reopenLedgerAccount(
        @PathVariable id: UUID,
        @Valid @RequestBody ledgerAccountActionDTO: LedgerAccountActionDTO
    ): ResponseEntity<Void> {
        log.debug("REST request to reopen LedgerAccount : $id")

        queryGateway.query(
            ReopenLedgerAccountCommand(id = id, comment = ledgerAccountActionDTO.comment),
            ResponseTypes.instanceOf(LedgerAccountView::class.java)
        )

        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME, id.toString()
                )
            ).build()
    }

    /**
     * `GET  /ledgerAccounts/:id/actions` : get all the actions of the "id" ledgerAccount.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of ledgerAccountAction in body.
     */
    @GetMapping("/{id}/actions")
    fun getAllLedgerAccountActions(id: UUID): CompletableFuture<MutableList<LedgerAccountActionView>>? {
        log.debug("REST request to get all actions of LedgerAccount : $id")

        return queryGateway.query(
            FindAllLedgerAccountActionsQuery(ledgerId = id),
            ResponseTypes.multipleInstancesOf(LedgerAccountActionView::class.java)
        )
    }

    /**
     * `GET  /ledgerAccounts/:id/entries` : get all the entries of the "id" ledgerAccount.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of ledgerAccountEntry in body.
     */
    @GetMapping("/{id}/entries")
    fun getAllLedgerAccountEntries(id: UUID): CompletableFuture<MutableList<LedgerAccountEntryView>>? {
        log.debug("REST request to get all entries of LedgerAccount : $id")

        return queryGateway.query(
            FindAllLedgerAccountEntriesQuery(ledgerId = id),
            ResponseTypes.multipleInstancesOf(LedgerAccountEntryView::class.java)
        )
    }

    /**
     *  `DELETE  /ledgerAccounts/:id` : delete the "id" ledgerAccount.
     *
     * @param id the id of the ledgerAccountDTO to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/{id}")
    fun deleteLedgerAccount(@PathVariable id: UUID): ResponseEntity<Void> {
        log.debug("REST request to delete LedgerAccount : $id")

        commandGateway.send<String>(DeleteLedgerAccountCommand(accountId = id))

        return ResponseEntity.noContent()
            .headers(
                HeaderUtil.createEntityDeletionAlert(
                    applicationName, true, ENTITY_NAME, id.toString())
            ).build()
    }
}
