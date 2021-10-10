package com.mycompany.myapp.service
import com.mycompany.myapp.domain.BorrowedBook
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

/**
 * Service Interface for managing [BorrowedBook].
 */
interface BorrowedBookService {

    /**
     * Save a borrowedBook.
     *
     * @param borrowedBook the entity to save.
     * @return the persisted entity.
     */
    fun save(borrowedBook: BorrowedBook): BorrowedBook

    /**
     * Partially updates a borrowedBook.
     *
     * @param borrowedBook the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(borrowedBook: BorrowedBook): Optional<BorrowedBook>

    /**
     * Get all the borrowedBooks.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<BorrowedBook>

    /**
     * Get the "id" borrowedBook.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<BorrowedBook>

    /**
     * Delete the "id" borrowedBook.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
