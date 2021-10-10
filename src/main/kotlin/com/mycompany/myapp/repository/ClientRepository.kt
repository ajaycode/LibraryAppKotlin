package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [Client] entity.
 */
@Suppress("unused")
@Repository
interface ClientRepository : JpaRepository<Client, Long>, JpaSpecificationExecutor<Client>
