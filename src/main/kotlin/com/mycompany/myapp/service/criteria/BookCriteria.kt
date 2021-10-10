package com.mycompany.myapp.service.criteria

import tech.jhipster.service.Criteria
import tech.jhipster.service.filter.IntegerFilter
import tech.jhipster.service.filter.LongFilter
import tech.jhipster.service.filter.StringFilter
import java.io.Serializable

/**
 * Criteria class for the [com.mycompany.myapp.domain.Book] entity. This class is used in
 * [com.mycompany.myapp.web.rest.BookResource] to receive all the possible filtering options from the
 * Http GET request parameters.
 * For example the following could be a valid request:
 * ```/books?id.greaterThan=5&attr1.contains=something&attr2.specified=false```
 * As Spring is unable to properly convert the types, unless specific [Filter] class are used, we need to use
 * fix type specific filters.
 */
data class BookCriteria(

    var id: LongFilter? = null,

    var isbn: StringFilter? = null,

    var name: StringFilter? = null,

    var publishYear: StringFilter? = null,

    var copies: IntegerFilter? = null,

    var publisherId: LongFilter? = null,

    var authorId: LongFilter? = null
) : Serializable, Criteria {

    constructor(other: BookCriteria) :
        this(
            other.id?.copy(),
            other.isbn?.copy(),
            other.name?.copy(),
            other.publishYear?.copy(),
            other.copies?.copy(),
            other.publisherId?.copy(),
            other.authorId?.copy()
        )

    override fun copy() = BookCriteria(this)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
