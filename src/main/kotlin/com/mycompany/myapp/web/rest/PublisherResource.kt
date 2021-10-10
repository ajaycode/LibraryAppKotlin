package com.mycompany.myapp.web.rest

import com.mycompany.myapp.domain.Publisher
import com.mycompany.myapp.repository.PublisherRepository
import com.mycompany.myapp.service.PublisherQueryService
import com.mycompany.myapp.service.PublisherService
import com.mycompany.myapp.service.criteria.PublisherCriteria
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

private const val ENTITY_NAME = "publisher"
/**
 * REST controller for managing [com.mycompany.myapp.domain.Publisher].
 */
@RestController
@RequestMapping("/api")
class PublisherResource(
    private val publisherService: PublisherService,
    private val publisherRepository: PublisherRepository,
    private val publisherQueryService: PublisherQueryService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "publisher"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /publishers` : Create a new publisher.
     *
     * @param publisher the publisher to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new publisher, or with status `400 (Bad Request)` if the publisher has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/publishers")
    fun createPublisher(@Valid @RequestBody publisher: Publisher): ResponseEntity<Publisher> {
        log.debug("REST request to save Publisher : $publisher")
        if (publisher.id != null) {
            throw BadRequestAlertException(
                "A new publisher cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = publisherService.save(publisher)
        return ResponseEntity.created(URI("/api/publishers/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /publishers/:id} : Updates an existing publisher.
     *
     * @param id the id of the publisher to save.
     * @param publisher the publisher to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated publisher,
     * or with status `400 (Bad Request)` if the publisher is not valid,
     * or with status `500 (Internal Server Error)` if the publisher couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/publishers/{id}")
    fun updatePublisher(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody publisher: Publisher
    ): ResponseEntity<Publisher> {
        log.debug("REST request to update Publisher : {}, {}", id, publisher)
        if (publisher.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, publisher.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!publisherRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = publisherService.save(publisher)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    publisher.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /publishers/:id} : Partial updates given fields of an existing publisher, field will ignore if it is null
     *
     * @param id the id of the publisher to save.
     * @param publisher the publisher to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated publisher,
     * or with status {@code 400 (Bad Request)} if the publisher is not valid,
     * or with status {@code 404 (Not Found)} if the publisher is not found,
     * or with status {@code 500 (Internal Server Error)} if the publisher couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/publishers/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdatePublisher(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody publisher: Publisher
    ): ResponseEntity<Publisher> {
        log.debug("REST request to partial update Publisher partially : {}, {}", id, publisher)
        if (publisher.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, publisher.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!publisherRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = publisherService.partialUpdate(publisher)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, publisher.id.toString())
        )
    }

    /**
     * `GET  /publishers` : get all the publishers.
     *
     * @param pageable the pagination information.

     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of publishers in body.
     */
    @GetMapping("/publishers") fun getAllPublishers(
        criteria: PublisherCriteria,
        pageable: Pageable

    ): ResponseEntity<MutableList<Publisher>> {
        log.debug("REST request to get Publishers by criteria: $criteria")
        val page = publisherQueryService.findByCriteria(criteria, pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /publishers/count}` : count all the publishers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the count in body.
     */
    @GetMapping("/publishers/count")
    fun countPublishers(criteria: PublisherCriteria): ResponseEntity<Long> {
        log.debug("REST request to count Publishers by criteria: $criteria")
        return ResponseEntity.ok().body(publisherQueryService.countByCriteria(criteria))
    }

    /**
     * `GET  /publishers/:id` : get the "id" publisher.
     *
     * @param id the id of the publisher to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the publisher, or with status `404 (Not Found)`.
     */
    @GetMapping("/publishers/{id}")
    fun getPublisher(@PathVariable id: Long): ResponseEntity<Publisher> {
        log.debug("REST request to get Publisher : $id")
        val publisher = publisherService.findOne(id)
        return ResponseUtil.wrapOrNotFound(publisher)
    }
    /**
     *  `DELETE  /publishers/:id` : delete the "id" publisher.
     *
     * @param id the id of the publisher to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/publishers/{id}")
    fun deletePublisher(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Publisher : $id")

        publisherService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
