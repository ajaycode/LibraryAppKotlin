package com.mycompany.myapp.service.impl

import com.mycompany.myapp.domain.Book
import com.mycompany.myapp.repository.BookRepository
import com.mycompany.myapp.service.BookService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * Service Implementation for managing [Book].
 */
@Service
@Transactional
class BookServiceImpl(
    private val bookRepository: BookRepository
) : BookService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(book: Book): Book {
        log.debug("Request to save Book : $book")
        return bookRepository.save(book)
    }

    override fun partialUpdate(book: Book): Optional<Book> {
        log.debug("Request to partially update Book : {}", book)

        return bookRepository.findById(book.id)
            .map {

                if (book.isbn != null) {
                    it.isbn = book.isbn
                }
                if (book.name != null) {
                    it.name = book.name
                }
                if (book.publishYear != null) {
                    it.publishYear = book.publishYear
                }
                if (book.copies != null) {
                    it.copies = book.copies
                }
                if (book.cover != null) {
                    it.cover = book.cover
                }
                if (book.coverContentType != null) {
                    it.coverContentType = book.coverContentType
                }

                it
            }
            .map { bookRepository.save(it) }
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<Book> {
        log.debug("Request to get all Books")
        return bookRepository.findAll(pageable)
    }

    override fun findAllWithEagerRelationships(pageable: Pageable) =
        bookRepository.findAllWithEagerRelationships(pageable)

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Book> {
        log.debug("Request to get Book : $id")
        return bookRepository.findOneWithEagerRelationships(id)
    }

    override fun delete(id: Long) {
        log.debug("Request to delete Book : $id")

        bookRepository.deleteById(id)
    }
}
