package com.mycompany.myapp.service.impl

import com.mycompany.myapp.domain.BorrowedBook
import com.mycompany.myapp.repository.BorrowedBookRepository
import com.mycompany.myapp.service.BorrowedBookService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * Service Implementation for managing [BorrowedBook].
 */
@Service
@Transactional
class BorrowedBookServiceImpl(
    private val borrowedBookRepository: BorrowedBookRepository
) : BorrowedBookService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(borrowedBook: BorrowedBook): BorrowedBook {
        log.debug("Request to save BorrowedBook : $borrowedBook")
        return borrowedBookRepository.save(borrowedBook)
    }

    override fun partialUpdate(borrowedBook: BorrowedBook): Optional<BorrowedBook> {
        log.debug("Request to partially update BorrowedBook : {}", borrowedBook)

        return borrowedBookRepository.findById(borrowedBook.id)
            .map {

                if (borrowedBook.borrowDate != null) {
                    it.borrowDate = borrowedBook.borrowDate
                }

                it
            }
            .map { borrowedBookRepository.save(it) }
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<BorrowedBook> {
        log.debug("Request to get all BorrowedBooks")
        return borrowedBookRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<BorrowedBook> {
        log.debug("Request to get BorrowedBook : $id")
        return borrowedBookRepository.findById(id)
    }

    override fun delete(id: Long) {
        log.debug("Request to delete BorrowedBook : $id")

        borrowedBookRepository.deleteById(id)
    }
}
