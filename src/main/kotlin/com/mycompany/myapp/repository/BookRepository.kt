package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * Spring Data SQL repository for the [Book] entity.
 */
@Repository
interface BookRepository : JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Query(
        value = "select distinct book from Book book left join fetch book.authors",
        countQuery = "select count(distinct book) from Book book"
    )
    fun findAllWithEagerRelationships(pageable: Pageable): Page<Book>

    @Query("select distinct book from Book book left join fetch book.authors")
    fun findAllWithEagerRelationships(): MutableList<Book>

    @Query("select book from Book book left join fetch book.authors where book.id =:id")
    fun findOneWithEagerRelationships(@Param("id") id: Long): Optional<Book>
}
