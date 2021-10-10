package com.mycompany.myapp.service
import com.mycompany.myapp.domain.Client
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

/**
 * Service Interface for managing [Client].
 */
interface ClientService {

    /**
     * Save a client.
     *
     * @param client the entity to save.
     * @return the persisted entity.
     */
    fun save(client: Client): Client

    /**
     * Partially updates a client.
     *
     * @param client the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(client: Client): Optional<Client>

    /**
     * Get all the clients.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Client>

    /**
     * Get the "id" client.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Client>

    /**
     * Delete the "id" client.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
