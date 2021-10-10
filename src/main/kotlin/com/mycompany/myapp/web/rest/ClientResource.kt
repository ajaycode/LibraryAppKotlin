package com.mycompany.myapp.web.rest

import com.mycompany.myapp.domain.Client
import com.mycompany.myapp.repository.ClientRepository
import com.mycompany.myapp.service.ClientQueryService
import com.mycompany.myapp.service.ClientService
import com.mycompany.myapp.service.criteria.ClientCriteria
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.PaginationUtil
import tech.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "client"
/**
 * REST controller for managing [com.mycompany.myapp.domain.Client].
 */
@RestController
@RequestMapping("/api")
class ClientResource(
    private val clientService: ClientService,
    private val clientRepository: ClientRepository,
    private val clientQueryService: ClientQueryService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "client"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /clients` : Create a new client.
     *
     * @param client the client to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new client, or with status `400 (Bad Request)` if the client has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/clients")
    fun createClient(@Valid @RequestBody client: Client): ResponseEntity<Client> {
        log.debug("REST request to save Client : $client")
        if (client.id != null) {
            throw BadRequestAlertException(
                "A new client cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = clientService.save(client)
        return ResponseEntity.created(URI("/api/clients/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /clients/:id} : Updates an existing client.
     *
     * @param id the id of the client to save.
     * @param client the client to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated client,
     * or with status `400 (Bad Request)` if the client is not valid,
     * or with status `500 (Internal Server Error)` if the client couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/clients/{id}")
    fun updateClient(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody client: Client
    ): ResponseEntity<Client> {
        log.debug("REST request to update Client : {}, {}", id, client)
        if (client.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, client.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!clientRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = clientService.save(client)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    client.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /clients/:id} : Partial updates given fields of an existing client, field will ignore if it is null
     *
     * @param id the id of the client to save.
     * @param client the client to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated client,
     * or with status {@code 400 (Bad Request)} if the client is not valid,
     * or with status {@code 404 (Not Found)} if the client is not found,
     * or with status {@code 500 (Internal Server Error)} if the client couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/clients/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateClient(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody client: Client
    ): ResponseEntity<Client> {
        log.debug("REST request to partial update Client partially : {}, {}", id, client)
        if (client.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, client.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!clientRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = clientService.partialUpdate(client)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, client.id.toString())
        )
    }

    /**
     * `GET  /clients` : get all the clients.
     *
     * @param pageable the pagination information.

     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of clients in body.
     */
    @GetMapping("/clients") fun getAllClients(
        criteria: ClientCriteria,
        pageable: Pageable

    ): ResponseEntity<MutableList<Client>> {
        log.debug("REST request to get Clients by criteria: $criteria")
        val page = clientQueryService.findByCriteria(criteria, pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /clients/count}` : count all the clients.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the count in body.
     */
    @GetMapping("/clients/count")
    fun countClients(criteria: ClientCriteria): ResponseEntity<Long> {
        log.debug("REST request to count Clients by criteria: $criteria")
        return ResponseEntity.ok().body(clientQueryService.countByCriteria(criteria))
    }

    /**
     * `GET  /clients/:id` : get the "id" client.
     *
     * @param id the id of the client to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the client, or with status `404 (Not Found)`.
     */
    @GetMapping("/clients/{id}")
    fun getClient(@PathVariable id: Long): ResponseEntity<Client> {
        log.debug("REST request to get Client : $id")
        val client = clientService.findOne(id)
        return ResponseUtil.wrapOrNotFound(client)
    }
    /**
     *  `DELETE  /clients/:id` : delete the "id" client.
     *
     * @param id the id of the client to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/clients/{id}")
    fun deleteClient(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Client : $id")

        clientService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
