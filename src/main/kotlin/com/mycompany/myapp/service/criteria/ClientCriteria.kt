package com.mycompany.myapp.service.criteria

import tech.jhipster.service.Criteria
import tech.jhipster.service.filter.LongFilter
import tech.jhipster.service.filter.StringFilter
import java.io.Serializable

/**
 * Criteria class for the [com.mycompany.myapp.domain.Client] entity. This class is used in
 * [com.mycompany.myapp.web.rest.ClientResource] to receive all the possible filtering options from the
 * Http GET request parameters.
 * For example the following could be a valid request:
 * ```/clients?id.greaterThan=5&attr1.contains=something&attr2.specified=false```
 * As Spring is unable to properly convert the types, unless specific [Filter] class are used, we need to use
 * fix type specific filters.
 */
data class ClientCriteria(

    var id: LongFilter? = null,

    var firstName: StringFilter? = null,

    var lastName: StringFilter? = null,

    var email: StringFilter? = null,

    var address: StringFilter? = null,

    var phone: StringFilter? = null
) : Serializable, Criteria {

    constructor(other: ClientCriteria) :
        this(
            other.id?.copy(),
            other.firstName?.copy(),
            other.lastName?.copy(),
            other.email?.copy(),
            other.address?.copy(),
            other.phone?.copy()
        )

    override fun copy() = ClientCriteria(this)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
