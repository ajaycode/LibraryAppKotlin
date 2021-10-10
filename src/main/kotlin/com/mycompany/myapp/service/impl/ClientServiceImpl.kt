package com.mycompany.myapp.service.impl

import com.mycompany.myapp.domain.Client
import com.mycompany.myapp.repository.ClientRepository
import com.mycompany.myapp.service.ClientService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * Service Implementation for managing [Client].
 */
@Service
@Transactional
class ClientServiceImpl(
    private val clientRepository: ClientRepository
) : ClientService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(client: Client): Client {
        log.debug("Request to save Client : $client")
        return clientRepository.save(client)
    }

    override fun partialUpdate(client: Client): Optional<Client> {
        log.debug("Request to partially update Client : {}", client)

        return clientRepository.findById(client.id)
            .map {

                if (client.firstName != null) {
                    it.firstName = client.firstName
                }
                if (client.lastName != null) {
                    it.lastName = client.lastName
                }
                if (client.email != null) {
                    it.email = client.email
                }
                if (client.address != null) {
                    it.address = client.address
                }
                if (client.phone != null) {
                    it.phone = client.phone
                }

                it
            }
            .map { clientRepository.save(it) }
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<Client> {
        log.debug("Request to get all Clients")
        return clientRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Client> {
        log.debug("Request to get Client : $id")
        return clientRepository.findById(id)
    }

    override fun delete(id: Long) {
        log.debug("Request to delete Client : $id")

        clientRepository.deleteById(id)
    }
}
