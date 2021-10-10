package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.PersistentToken
import com.mycompany.myapp.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

/**
 * Spring Data JPA repository for the [PersistentToken] entity.
 */
interface PersistentTokenRepository : JpaRepository<PersistentToken, String> {
    fun findByUser(user: User): List<PersistentToken>

    fun findByTokenDateBefore(localDate: LocalDate): List<PersistentToken>
}
