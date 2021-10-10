package com.mycompany.myapp.service

import com.mycompany.myapp.domain.Book_
import com.mycompany.myapp.domain.BorrowedBook
import com.mycompany.myapp.domain.BorrowedBook_
import com.mycompany.myapp.domain.Client_
import com.mycompany.myapp.repository.BorrowedBookRepository
import com.mycompany.myapp.service.criteria.BorrowedBookCriteria
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.jhipster.service.QueryService
import tech.jhipster.service.filter.Filter
import javax.persistence.criteria.JoinType

/**
 * Service for executing complex queries for [BorrowedBook] entities in the database.
 * The main input is a [BorrowedBookCriteria] which gets converted to [Specification],
 * in a way that all the filters must apply.
 * It returns a [MutableList] of [BorrowedBook] or a [Page] of [BorrowedBook] which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
class BorrowedBookQueryService(
    private val borrowedBookRepository: BorrowedBookRepository
) : QueryService<BorrowedBook>() {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Return a [MutableList] of [BorrowedBook] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: BorrowedBookCriteria?): MutableList<BorrowedBook> {
        log.debug("find by criteria : $criteria")
        val specification = createSpecification(criteria)
        return borrowedBookRepository.findAll(specification)
    }

    /**
     * Return a [Page] of [BorrowedBook] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: BorrowedBookCriteria?, page: Pageable): Page<BorrowedBook> {
        log.debug("find by criteria : $criteria, page: $page")
        val specification = createSpecification(criteria)
        return borrowedBookRepository.findAll(specification, page)
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    fun countByCriteria(criteria: BorrowedBookCriteria?): Long {
        log.debug("count by criteria : $criteria")
        val specification = createSpecification(criteria)
        return borrowedBookRepository.count(specification)
    }

    /**
     * Function to convert [BorrowedBookCriteria] to a [Specification].
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching [Specification] of the entity.
     */
    protected fun createSpecification(criteria: BorrowedBookCriteria?): Specification<BorrowedBook?> {
        var specification: Specification<BorrowedBook?> = Specification.where(null)
        if (criteria != null) {
            if (criteria.id != null) {
                specification = specification.and(buildRangeSpecification(criteria.id, BorrowedBook_.id))
            }
            if (criteria.borrowDate != null) {
                specification = specification.and(buildRangeSpecification(criteria.borrowDate, BorrowedBook_.borrowDate))
            }
            if (criteria.bookId != null) {
                specification = specification.and(
                    buildSpecification(criteria.bookId as Filter<Long>) {
                        it.join(BorrowedBook_.book, JoinType.LEFT).get(Book_.id)
                    }
                )
            }
            if (criteria.clientId != null) {
                specification = specification.and(
                    buildSpecification(criteria.clientId as Filter<Long>) {
                        it.join(BorrowedBook_.client, JoinType.LEFT).get(Client_.id)
                    }
                )
            }
        }
        return specification
    }
}
