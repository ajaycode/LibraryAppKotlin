package com.mycompany.myapp.repository

import com.mycompany.myapp.domain.Publisher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [Publisher] entity.
 */
@Suppress("unused")
@Repository
interface PublisherRepository : JpaRepository<Publisher, Long>, JpaSpecificationExecutor<Publisher>
