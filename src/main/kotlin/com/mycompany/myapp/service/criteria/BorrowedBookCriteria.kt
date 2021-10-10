package com.mycompany.myapp.service.criteria

import tech.jhipster.service.Criteria
import tech.jhipster.service.filter.LocalDateFilter
import tech.jhipster.service.filter.LongFilter
import java.io.Serializable

/**
 * Criteria class for the [com.mycompany.myapp.domain.BorrowedBook] entity. This class is used in
 * [com.mycompany.myapp.web.rest.BorrowedBookResource] to receive all the possible filtering options from the
 * Http GET request parameters.
 * For example the following could be a valid request:
 * ```/borrowed-books?id.greaterThan=5&attr1.contains=something&attr2.specified=false```
 * As Spring is unable to properly convert the types, unless specific [Filter] class are used, we need to use
 * fix type specific filters.
 */
data class BorrowedBookCriteria(

    var id: LongFilter? = null,

    var borrowDate: LocalDateFilter? = null,

    var bookId: LongFilter? = null,

    var clientId: LongFilter? = null
) : Serializable, Criteria {

    constructor(other: BorrowedBookCriteria) :
        this(
            other.id?.copy(),
            other.borrowDate?.copy(),
            other.bookId?.copy(),
            other.clientId?.copy()
        )

    override fun copy() = BorrowedBookCriteria(this)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
