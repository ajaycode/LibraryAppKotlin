package com.mycompany.myapp.service.impl

import com.mycompany.myapp.domain.Publisher
import com.mycompany.myapp.repository.PublisherRepository
import com.mycompany.myapp.service.PublisherService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * Service Implementation for managing [Publisher].
 */
@Service
@Transactional
class PublisherServiceImpl(
    private val publisherRepository: PublisherRepository
) : PublisherService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(publisher: Publisher): Publisher {
        log.debug("Request to save Publisher : $publisher")
        return publisherRepository.save(publisher)
    }

    override fun partialUpdate(publisher: Publisher): Optional<Publisher> {
        log.debug("Request to partially update Publisher : {}", publisher)

        return publisherRepository.findById(publisher.id)
            .map {

                if (publisher.name != null) {
                    it.name = publisher.name
                }

                it
            }
            .map { publisherRepository.save(it) }
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<Publisher> {
        log.debug("Request to get all Publishers")
        return publisherRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Publisher> {
        log.debug("Request to get Publisher : $id")
        return publisherRepository.findById(id)
    }

    override fun delete(id: Long) {
        log.debug("Request to delete Publisher : $id")

        publisherRepository.deleteById(id)
    }
}
