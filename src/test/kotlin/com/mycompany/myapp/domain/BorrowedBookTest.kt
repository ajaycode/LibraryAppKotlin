package com.mycompany.myapp.domain

import com.mycompany.myapp.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BorrowedBookTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(BorrowedBook::class)
        val borrowedBook1 = BorrowedBook()
        borrowedBook1.id = 1L
        val borrowedBook2 = BorrowedBook()
        borrowedBook2.id = borrowedBook1.id
        assertThat(borrowedBook1).isEqualTo(borrowedBook2)
        borrowedBook2.id = 2L
        assertThat(borrowedBook1).isNotEqualTo(borrowedBook2)
        borrowedBook1.id = null
        assertThat(borrowedBook1).isNotEqualTo(borrowedBook2)
    }
}
