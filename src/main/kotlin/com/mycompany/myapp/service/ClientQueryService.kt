package com.mycompany.myapp.service

import com.mycompany.myapp.domain.Client
import com.mycompany.myapp.domain.Client_
import com.mycompany.myapp.repository.ClientRepository
import com.mycompany.myapp.service.criteria.ClientCriteria
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.jhipster.service.QueryService

/**
 * Service for executing complex queries for [Client] entities in the database.
 * The main input is a [ClientCriteria] which gets converted to [Specification],
 * in a way that all the filters must apply.
 * It returns a [MutableList] of [Client] or a [Page] of [Client] which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
class ClientQueryService(
    private val clientRepository: ClientRepository
) : QueryService<Client>() {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Return a [MutableList] of [Client] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: ClientCriteria?): MutableList<Client> {
        log.debug("find by criteria : $criteria")
        val specification = createSpecification(criteria)
        return clientRepository.findAll(specification)
    }

    /**
     * Return a [Page] of [Client] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: ClientCriteria?, page: Pageable): Page<Client> {
        log.debug("find by criteria : $criteria, page: $page")
        val specification = createSpecification(criteria)
        return clientRepository.findAll(specification, page)
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    fun countByCriteria(criteria: ClientCriteria?): Long {
        log.debug("count by criteria : $criteria")
        val specification = createSpecification(criteria)
        return clientRepository.count(specification)
    }

    /**
     * Function to convert [ClientCriteria] to a [Specification].
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching [Specification] of the entity.
     */
    protected fun createSpecification(criteria: ClientCriteria?): Specification<Client?> {
        var specification: Specification<Client?> = Specification.where(null)
        if (criteria != null) {
            if (criteria.id != null) {
                specification = specification.and(buildRangeSpecification(criteria.id, Client_.id))
            }
            if (criteria.firstName != null) {
                specification = specification.and(buildStringSpecification(criteria.firstName, Client_.firstName))
            }
            if (criteria.lastName != null) {
                specification = specification.and(buildStringSpecification(criteria.lastName, Client_.lastName))
            }
            if (criteria.email != null) {
                specification = specification.and(buildStringSpecification(criteria.email, Client_.email))
            }
            if (criteria.address != null) {
                specification = specification.and(buildStringSpecification(criteria.address, Client_.address))
            }
            if (criteria.phone != null) {
                specification = specification.and(buildStringSpecification(criteria.phone, Client_.phone))
            }
        }
        return specification
    }
}
