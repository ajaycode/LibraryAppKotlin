package com.mycompany.myapp.service.criteria

import tech.jhipster.service.Criteria
import tech.jhipster.service.filter.LongFilter
import tech.jhipster.service.filter.StringFilter
import java.io.Serializable

/**
 * Criteria class for the [com.mycompany.myapp.domain.Publisher] entity. This class is used in
 * [com.mycompany.myapp.web.rest.PublisherResource] to receive all the possible filtering options from the
 * Http GET request parameters.
 * For example the following could be a valid request:
 * ```/publishers?id.greaterThan=5&attr1.contains=something&attr2.specified=false```
 * As Spring is unable to properly convert the types, unless specific [Filter] class are used, we need to use
 * fix type specific filters.
 */
data class PublisherCriteria(

    var id: LongFilter? = null,

    var name: StringFilter? = null
) : Serializable, Criteria {

    constructor(other: PublisherCriteria) :
        this(
            other.id?.copy(),
            other.name?.copy()
        )

    override fun copy() = PublisherCriteria(this)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
