package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.Author
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [Author] entity.
 */
@Suppress("unused")
@Repository
interface AuthorRepository : JpaRepository<Author, Long>, JpaSpecificationExecutor<Author>
