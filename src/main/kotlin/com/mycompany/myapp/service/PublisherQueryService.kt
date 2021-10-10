package com.mycompany.myapp.service

import com.mycompany.myapp.domain.Publisher
import com.mycompany.myapp.domain.Publisher_
import com.mycompany.myapp.repository.PublisherRepository
import com.mycompany.myapp.service.criteria.PublisherCriteria
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.jhipster.service.QueryService

/**
 * Service for executing complex queries for [Publisher] entities in the database.
 * The main input is a [PublisherCriteria] which gets converted to [Specification],
 * in a way that all the filters must apply.
 * It returns a [MutableList] of [Publisher] or a [Page] of [Publisher] which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
class PublisherQueryService(
    private val publisherRepository: PublisherRepository
) : QueryService<Publisher>() {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Return a [MutableList] of [Publisher] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: PublisherCriteria?): MutableList<Publisher> {
        log.debug("find by criteria : $criteria")
        val specification = createSpecification(criteria)
        return publisherRepository.findAll(specification)
    }

    /**
     * Return a [Page] of [Publisher] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: PublisherCriteria?, page: Pageable): Page<Publisher> {
        log.debug("find by criteria : $criteria, page: $page")
        val specification = createSpecification(criteria)
        return publisherRepository.findAll(specification, page)
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    fun countByCriteria(criteria: PublisherCriteria?): Long {
        log.debug("count by criteria : $criteria")
        val specification = createSpecification(criteria)
        return publisherRepository.count(specification)
    }

    /**
     * Function to convert [PublisherCriteria] to a [Specification].
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching [Specification] of the entity.
     */
    protected fun createSpecification(criteria: PublisherCriteria?): Specification<Publisher?> {
        var specification: Specification<Publisher?> = Specification.where(null)
        if (criteria != null) {
            if (criteria.id != null) {
                specification = specification.and(buildRangeSpecification(criteria.id, Publisher_.id))
            }
            if (criteria.name != null) {
                specification = specification.and(buildStringSpecification(criteria.name, Publisher_.name))
            }
        }
        return specification
    }
}
