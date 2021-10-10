package com.mycompany.myapp.service
import com.mycompany.myapp.domain.Publisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

/**
 * Service Interface for managing [Publisher].
 */
interface PublisherService {

    /**
     * Save a publisher.
     *
     * @param publisher the entity to save.
     * @return the persisted entity.
     */
    fun save(publisher: Publisher): Publisher

    /**
     * Partially updates a publisher.
     *
     * @param publisher the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(publisher: Publisher): Optional<Publisher>

    /**
     * Get all the publishers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Publisher>

    /**
     * Get the "id" publisher.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Publisher>

    /**
     * Delete the "id" publisher.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
