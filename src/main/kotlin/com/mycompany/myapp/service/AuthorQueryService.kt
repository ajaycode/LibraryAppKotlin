package com.mycompany.myapp.service

import com.mycompany.myapp.domain.Author
import com.mycompany.myapp.domain.Author_
import com.mycompany.myapp.domain.Book_
import com.mycompany.myapp.repository.AuthorRepository
import com.mycompany.myapp.service.criteria.AuthorCriteria
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
 * Service for executing complex queries for [Author] entities in the database.
 * The main input is a [AuthorCriteria] which gets converted to [Specification],
 * in a way that all the filters must apply.
 * It returns a [MutableList] of [Author] or a [Page] of [Author] which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
class AuthorQueryService(
    private val authorRepository: AuthorRepository
) : QueryService<Author>() {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Return a [MutableList] of [Author] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: AuthorCriteria?): MutableList<Author> {
        log.debug("find by criteria : $criteria")
        val specification = createSpecification(criteria)
        return authorRepository.findAll(specification)
    }

    /**
     * Return a [Page] of [Author] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: AuthorCriteria?, page: Pageable): Page<Author> {
        log.debug("find by criteria : $criteria, page: $page")
        val specification = createSpecification(criteria)
        return authorRepository.findAll(specification, page)
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    fun countByCriteria(criteria: AuthorCriteria?): Long {
        log.debug("count by criteria : $criteria")
        val specification = createSpecification(criteria)
        return authorRepository.count(specification)
    }

    /**
     * Function to convert [AuthorCriteria] to a [Specification].
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching [Specification] of the entity.
     */
    protected fun createSpecification(criteria: AuthorCriteria?): Specification<Author?> {
        var specification: Specification<Author?> = Specification.where(null)
        if (criteria != null) {
            if (criteria.id != null) {
                specification = specification.and(buildRangeSpecification(criteria.id, Author_.id))
            }
            if (criteria.firstName != null) {
                specification = specification.and(buildStringSpecification(criteria.firstName, Author_.firstName))
            }
            if (criteria.lastName != null) {
                specification = specification.and(buildStringSpecification(criteria.lastName, Author_.lastName))
            }
            if (criteria.bookId != null) {
                specification = specification.and(
                    buildSpecification(criteria.bookId as Filter<Long>) {
                        it.join(Author_.books, JoinType.LEFT).get(Book_.id)
                    }
                )
            }
        }
        return specification
    }
}
