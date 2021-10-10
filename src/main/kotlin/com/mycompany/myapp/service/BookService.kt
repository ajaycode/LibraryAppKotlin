package com.mycompany.myapp.service
import com.mycompany.myapp.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

/**
 * Service Interface for managing [Book].
 */
interface BookService {

    /**
     * Save a book.
     *
     * @param book the entity to save.
     * @return the persisted entity.
     */
    fun save(book: Book): Book

    /**
     * Partially updates a book.
     *
     * @param book the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(book: Book): Optional<Book>

    /**
     * Get all the books.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Book>

    /**
     * Get all the books with eager load of many-to-many relationships.
     * @param pageable the pagination information.
     *
     * @return the list of entities.
     */
    fun findAllWithEagerRelationships(pageable: Pageable): Page<Book>
    /**
     * Get the "id" book.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Book>

    /**
     * Delete the "id" book.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
