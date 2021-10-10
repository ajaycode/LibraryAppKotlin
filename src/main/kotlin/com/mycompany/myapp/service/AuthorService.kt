package com.mycompany.myapp.service
import com.mycompany.myapp.domain.Author
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

/**
 * Service Interface for managing [Author].
 */
interface AuthorService {

    /**
     * Save a author.
     *
     * @param author the entity to save.
     * @return the persisted entity.
     */
    fun save(author: Author): Author

    /**
     * Partially updates a author.
     *
     * @param author the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(author: Author): Optional<Author>

    /**
     * Get all the authors.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Author>

    /**
     * Get the "id" author.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Author>

    /**
     * Delete the "id" author.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
