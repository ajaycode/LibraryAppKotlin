package com.mycompany.myapp.web.rest

import com.mycompany.myapp.domain.Author
import com.mycompany.myapp.repository.AuthorRepository
import com.mycompany.myapp.service.AuthorQueryService
import com.mycompany.myapp.service.AuthorService
import com.mycompany.myapp.service.criteria.AuthorCriteria
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

private const val ENTITY_NAME = "author"
/**
 * REST controller for managing [com.mycompany.myapp.domain.Author].
 */
@RestController
@RequestMapping("/api")
class AuthorResource(
    private val authorService: AuthorService,
    private val authorRepository: AuthorRepository,
    private val authorQueryService: AuthorQueryService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "author"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /authors` : Create a new author.
     *
     * @param author the author to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new author, or with status `400 (Bad Request)` if the author has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/authors")
    fun createAuthor(@Valid @RequestBody author: Author): ResponseEntity<Author> {
        log.debug("REST request to save Author : $author")
        if (author.id != null) {
            throw BadRequestAlertException(
                "A new author cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = authorService.save(author)
        return ResponseEntity.created(URI("/api/authors/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /authors/:id} : Updates an existing author.
     *
     * @param id the id of the author to save.
     * @param author the author to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated author,
     * or with status `400 (Bad Request)` if the author is not valid,
     * or with status `500 (Internal Server Error)` if the author couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/authors/{id}")
    fun updateAuthor(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody author: Author
    ): ResponseEntity<Author> {
        log.debug("REST request to update Author : {}, {}", id, author)
        if (author.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, author.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!authorRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = authorService.save(author)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    author.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /authors/:id} : Partial updates given fields of an existing author, field will ignore if it is null
     *
     * @param id the id of the author to save.
     * @param author the author to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated author,
     * or with status {@code 400 (Bad Request)} if the author is not valid,
     * or with status {@code 404 (Not Found)} if the author is not found,
     * or with status {@code 500 (Internal Server Error)} if the author couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/authors/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateAuthor(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody author: Author
    ): ResponseEntity<Author> {
        log.debug("REST request to partial update Author partially : {}, {}", id, author)
        if (author.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, author.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!authorRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = authorService.partialUpdate(author)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, author.id.toString())
        )
    }

    /**
     * `GET  /authors` : get all the authors.
     *
     * @param pageable the pagination information.

     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of authors in body.
     */
    @GetMapping("/authors") fun getAllAuthors(
        criteria: AuthorCriteria,
        pageable: Pageable

    ): ResponseEntity<MutableList<Author>> {
        log.debug("REST request to get Authors by criteria: $criteria")
        val page = authorQueryService.findByCriteria(criteria, pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /authors/count}` : count all the authors.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the count in body.
     */
    @GetMapping("/authors/count")
    fun countAuthors(criteria: AuthorCriteria): ResponseEntity<Long> {
        log.debug("REST request to count Authors by criteria: $criteria")
        return ResponseEntity.ok().body(authorQueryService.countByCriteria(criteria))
    }

    /**
     * `GET  /authors/:id` : get the "id" author.
     *
     * @param id the id of the author to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the author, or with status `404 (Not Found)`.
     */
    @GetMapping("/authors/{id}")
    fun getAuthor(@PathVariable id: Long): ResponseEntity<Author> {
        log.debug("REST request to get Author : $id")
        val author = authorService.findOne(id)
        return ResponseUtil.wrapOrNotFound(author)
    }
    /**
     *  `DELETE  /authors/:id` : delete the "id" author.
     *
     * @param id the id of the author to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/authors/{id}")
    fun deleteAuthor(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Author : $id")

        authorService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
