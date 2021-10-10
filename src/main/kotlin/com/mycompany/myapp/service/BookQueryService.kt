package com.mycompany.myapp.service

import com.mycompany.myapp.domain.Author_
import com.mycompany.myapp.domain.Book
import com.mycompany.myapp.domain.Book_
import com.mycompany.myapp.domain.Publisher_
import com.mycompany.myapp.repository.BookRepository
import com.mycompany.myapp.service.criteria.BookCriteria
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
 * Service for executing complex queries for [Book] entities in the database.
 * The main input is a [BookCriteria] which gets converted to [Specification],
 * in a way that all the filters must apply.
 * It returns a [MutableList] of [Book] or a [Page] of [Book] which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
class BookQueryService(
    private val bookRepository: BookRepository
) : QueryService<Book>() {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Return a [MutableList] of [Book] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: BookCriteria?): MutableList<Book> {
        log.debug("find by criteria : $criteria")
        val specification = createSpecification(criteria)
        return bookRepository.findAll(specification)
    }

    /**
     * Return a [Page] of [Book] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: BookCriteria?, page: Pageable): Page<Book> {
        log.debug("find by criteria : $criteria, page: $page")
        val specification = createSpecification(criteria)
        return bookRepository.findAll(specification, page)
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    fun countByCriteria(criteria: BookCriteria?): Long {
        log.debug("count by criteria : $criteria")
        val specification = createSpecification(criteria)
        return bookRepository.count(specification)
    }

    /**
     * Function to convert [BookCriteria] to a [Specification].
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching [Specification] of the entity.
     */
    protected fun createSpecification(criteria: BookCriteria?): Specification<Book?> {
        var specification: Specification<Book?> = Specification.where(null)
        if (criteria != null) {
            if (criteria.id != null) {
                specification = specification.and(buildRangeSpecification(criteria.id, Book_.id))
            }
            if (criteria.isbn != null) {
                specification = specification.and(buildStringSpecification(criteria.isbn, Book_.isbn))
            }
            if (criteria.name != null) {
                specification = specification.and(buildStringSpecification(criteria.name, Book_.name))
            }
            if (criteria.publishYear != null) {
                specification = specification.and(buildStringSpecification(criteria.publishYear, Book_.publishYear))
            }
            if (criteria.copies != null) {
                specification = specification.and(buildRangeSpecification(criteria.copies, Book_.copies))
            }
            if (criteria.publisherId != null) {
                specification = specification.and(
                    buildSpecification(criteria.publisherId as Filter<Long>) {
                        it.join(Book_.publisher, JoinType.LEFT).get(Publisher_.id)
                    }
                )
            }
            if (criteria.authorId != null) {
                specification = specification.and(
                    buildSpecification(criteria.authorId as Filter<Long>) {
                        it.join(Book_.authors, JoinType.LEFT).get(Author_.id)
                    }
                )
            }
        }
        return specification
    }
}
