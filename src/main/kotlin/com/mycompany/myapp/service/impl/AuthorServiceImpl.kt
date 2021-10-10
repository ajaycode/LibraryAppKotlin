package com.mycompany.myapp.service.impl

import com.mycompany.myapp.domain.Author
import com.mycompany.myapp.repository.AuthorRepository
import com.mycompany.myapp.service.AuthorService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * Service Implementation for managing [Author].
 */
@Service
@Transactional
class AuthorServiceImpl(
    private val authorRepository: AuthorRepository
) : AuthorService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(author: Author): Author {
        log.debug("Request to save Author : $author")
        return authorRepository.save(author)
    }

    override fun partialUpdate(author: Author): Optional<Author> {
        log.debug("Request to partially update Author : {}", author)

        return authorRepository.findById(author.id)
            .map {

                if (author.firstName != null) {
                    it.firstName = author.firstName
                }
                if (author.lastName != null) {
                    it.lastName = author.lastName
                }

                it
            }
            .map { authorRepository.save(it) }
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<Author> {
        log.debug("Request to get all Authors")
        return authorRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Author> {
        log.debug("Request to get Author : $id")
        return authorRepository.findById(id)
    }

    override fun delete(id: Long) {
        log.debug("Request to delete Author : $id")

        authorRepository.deleteById(id)
    }
}
