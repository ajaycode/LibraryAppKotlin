package com.mycompany.myapp.web.rest

import com.mycompany.myapp.domain.BorrowedBook
import com.mycompany.myapp.repository.BorrowedBookRepository
import com.mycompany.myapp.service.BorrowedBookQueryService
import com.mycompany.myapp.service.BorrowedBookService
import com.mycompany.myapp.service.criteria.BorrowedBookCriteria
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

private const val ENTITY_NAME = "borrowedBook"
/**
 * REST controller for managing [com.mycompany.myapp.domain.BorrowedBook].
 */
@RestController
@RequestMapping("/api")
class BorrowedBookResource(
    private val borrowedBookService: BorrowedBookService,
    private val borrowedBookRepository: BorrowedBookRepository,
    private val borrowedBookQueryService: BorrowedBookQueryService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "borrowedBook"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /borrowed-books` : Create a new borrowedBook.
     *
     * @param borrowedBook the borrowedBook to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new borrowedBook, or with status `400 (Bad Request)` if the borrowedBook has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/borrowed-books")
    fun createBorrowedBook(@RequestBody borrowedBook: BorrowedBook): ResponseEntity<BorrowedBook> {
        log.debug("REST request to save BorrowedBook : $borrowedBook")
        if (borrowedBook.id != null) {
            throw BadRequestAlertException(
                "A new borrowedBook cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = borrowedBookService.save(borrowedBook)
        return ResponseEntity.created(URI("/api/borrowed-books/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /borrowed-books/:id} : Updates an existing borrowedBook.
     *
     * @param id the id of the borrowedBook to save.
     * @param borrowedBook the borrowedBook to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated borrowedBook,
     * or with status `400 (Bad Request)` if the borrowedBook is not valid,
     * or with status `500 (Internal Server Error)` if the borrowedBook couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/borrowed-books/{id}")
    fun updateBorrowedBook(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody borrowedBook: BorrowedBook
    ): ResponseEntity<BorrowedBook> {
        log.debug("REST request to update BorrowedBook : {}, {}", id, borrowedBook)
        if (borrowedBook.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, borrowedBook.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!borrowedBookRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = borrowedBookService.save(borrowedBook)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    borrowedBook.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /borrowed-books/:id} : Partial updates given fields of an existing borrowedBook, field will ignore if it is null
     *
     * @param id the id of the borrowedBook to save.
     * @param borrowedBook the borrowedBook to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated borrowedBook,
     * or with status {@code 400 (Bad Request)} if the borrowedBook is not valid,
     * or with status {@code 404 (Not Found)} if the borrowedBook is not found,
     * or with status {@code 500 (Internal Server Error)} if the borrowedBook couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/borrowed-books/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateBorrowedBook(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody borrowedBook: BorrowedBook
    ): ResponseEntity<BorrowedBook> {
        log.debug("REST request to partial update BorrowedBook partially : {}, {}", id, borrowedBook)
        if (borrowedBook.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, borrowedBook.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!borrowedBookRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = borrowedBookService.partialUpdate(borrowedBook)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, borrowedBook.id.toString())
        )
    }

    /**
     * `GET  /borrowed-books` : get all the borrowedBooks.
     *
     * @param pageable the pagination information.

     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of borrowedBooks in body.
     */
    @GetMapping("/borrowed-books") fun getAllBorrowedBooks(
        criteria: BorrowedBookCriteria,
        pageable: Pageable

    ): ResponseEntity<MutableList<BorrowedBook>> {
        log.debug("REST request to get BorrowedBooks by criteria: $criteria")
        val page = borrowedBookQueryService.findByCriteria(criteria, pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /borrowed-books/count}` : count all the borrowedBooks.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the count in body.
     */
    @GetMapping("/borrowed-books/count")
    fun countBorrowedBooks(criteria: BorrowedBookCriteria): ResponseEntity<Long> {
        log.debug("REST request to count BorrowedBooks by criteria: $criteria")
        return ResponseEntity.ok().body(borrowedBookQueryService.countByCriteria(criteria))
    }

    /**
     * `GET  /borrowed-books/:id` : get the "id" borrowedBook.
     *
     * @param id the id of the borrowedBook to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the borrowedBook, or with status `404 (Not Found)`.
     */
    @GetMapping("/borrowed-books/{id}")
    fun getBorrowedBook(@PathVariable id: Long): ResponseEntity<BorrowedBook> {
        log.debug("REST request to get BorrowedBook : $id")
        val borrowedBook = borrowedBookService.findOne(id)
        return ResponseUtil.wrapOrNotFound(borrowedBook)
    }
    /**
     *  `DELETE  /borrowed-books/:id` : delete the "id" borrowedBook.
     *
     * @param id the id of the borrowedBook to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/borrowed-books/{id}")
    fun deleteBorrowedBook(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete BorrowedBook : $id")

        borrowedBookService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
