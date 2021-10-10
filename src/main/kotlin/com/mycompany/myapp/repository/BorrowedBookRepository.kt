package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.BorrowedBook
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [BorrowedBook] entity.
 */
@Suppress("unused")
@Repository
interface BorrowedBookRepository : JpaRepository<BorrowedBook, Long>, JpaSpecificationExecutor<BorrowedBook>
