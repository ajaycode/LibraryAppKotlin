package com.mycompany.myapp.web.rest

import com.mycompany.myapp.IntegrationTest
import com.mycompany.myapp.domain.Publisher
import com.mycompany.myapp.repository.PublisherRepository
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [PublisherResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PublisherResourceIT {
    @Autowired
    private lateinit var publisherRepository: PublisherRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var restPublisherMockMvc: MockMvc

    private lateinit var publisher: Publisher

    @BeforeEach
    fun initTest() {
        publisher = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createPublisher() {
        val databaseSizeBeforeCreate = publisherRepository.findAll().size

        // Create the Publisher
        restPublisherMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(publisher))
        ).andExpect(status().isCreated)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeCreate + 1)
        val testPublisher = publisherList[publisherList.size - 1]

        assertThat(testPublisher.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createPublisherWithExistingId() {
        // Create the Publisher with an existing ID
        publisher.id = 1L

        val databaseSizeBeforeCreate = publisherRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restPublisherMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(publisher))
        ).andExpect(status().isBadRequest)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = publisherRepository.findAll().size
        // set the field null
        publisher.name = null

        // Create the Publisher, which fails.

        restPublisherMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(publisher))
        ).andExpect(status().isBadRequest)

        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublishers() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        // Get all the publisherList
        restPublisherMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(publisher.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getPublisher() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        val id = publisher.id
        assertNotNull(id)

        // Get the publisher
        restPublisherMockMvc.perform(get(ENTITY_API_URL_ID, publisher.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(publisher.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getPublishersByIdFiltering() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)
        val id = publisher.id

        defaultPublisherShouldBeFound("id.equals=" + id)
        defaultPublisherShouldNotBeFound("id.notEquals=" + id)
        defaultPublisherShouldBeFound("id.greaterThanOrEqual=" + id)
        defaultPublisherShouldNotBeFound("id.greaterThan=" + id)

        defaultPublisherShouldBeFound("id.lessThanOrEqual=" + id)
        defaultPublisherShouldNotBeFound("id.lessThan=" + id)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublishersByNameIsEqualToSomething() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        // Get all the publisherList where name equals to DEFAULT_NAME
        defaultPublisherShouldBeFound("name.equals=$DEFAULT_NAME")

        // Get all the publisherList where name equals to UPDATED_NAME
        defaultPublisherShouldNotBeFound("name.equals=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublishersByNameIsNotEqualToSomething() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        // Get all the publisherList where name not equals to DEFAULT_NAME
        defaultPublisherShouldNotBeFound("name.notEquals=" + DEFAULT_NAME)

        // Get all the publisherList where name not equals to UPDATED_NAME
        defaultPublisherShouldBeFound("name.notEquals=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublishersByNameIsInShouldWork() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        // Get all the publisherList where name in DEFAULT_NAME or UPDATED_NAME
        defaultPublisherShouldBeFound("name.in=$DEFAULT_NAME,$UPDATED_NAME")

        // Get all the publisherList where name equals to UPDATED_NAME
        defaultPublisherShouldNotBeFound("name.in=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublishersByNameIsNullOrNotNull() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        // Get all the publisherList where name is not null
        defaultPublisherShouldBeFound("name.specified=true")

        // Get all the publisherList where name is null
        defaultPublisherShouldNotBeFound("name.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublishersByNameContainsSomething() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        // Get all the publisherList where name contains DEFAULT_NAME
        defaultPublisherShouldBeFound("name.contains=" + DEFAULT_NAME)

        // Get all the publisherList where name contains UPDATED_NAME
        defaultPublisherShouldNotBeFound("name.contains=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublishersByNameNotContainsSomething() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        // Get all the publisherList where name does not contain DEFAULT_NAME
        defaultPublisherShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME)

        // Get all the publisherList where name does not contain UPDATED_NAME
        defaultPublisherShouldBeFound("name.doesNotContain=" + UPDATED_NAME)
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */

    @Throws(Exception::class)
    private fun defaultPublisherShouldBeFound(filter: String) {
        restPublisherMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(publisher.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))

        // Check, that the count call also returns 1
        restPublisherMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"))
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    @Throws(Exception::class)
    private fun defaultPublisherShouldNotBeFound(filter: String) {
        restPublisherMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Check, that the count call also returns 0
        restPublisherMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingPublisher() {
        // Get the publisher
        restPublisherMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewPublisher() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        val databaseSizeBeforeUpdate = publisherRepository.findAll().size

        // Update the publisher
        val updatedPublisher = publisherRepository.findById(publisher.id).get()
        // Disconnect from session so that the updates on updatedPublisher are not directly saved in db
        em.detach(updatedPublisher)
        updatedPublisher.name = UPDATED_NAME

        restPublisherMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedPublisher.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedPublisher))
        ).andExpect(status().isOk)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
        val testPublisher = publisherList[publisherList.size - 1]
        assertThat(testPublisher.name).isEqualTo(UPDATED_NAME)
    }

    @Test
    @Transactional
    fun putNonExistingPublisher() {
        val databaseSizeBeforeUpdate = publisherRepository.findAll().size
        publisher.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPublisherMockMvc.perform(
            put(ENTITY_API_URL_ID, publisher.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(publisher))
        )
            .andExpect(status().isBadRequest)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchPublisher() {
        val databaseSizeBeforeUpdate = publisherRepository.findAll().size
        publisher.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPublisherMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(publisher))
        ).andExpect(status().isBadRequest)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamPublisher() {
        val databaseSizeBeforeUpdate = publisherRepository.findAll().size
        publisher.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPublisherMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(publisher))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdatePublisherWithPatch() {

        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        val databaseSizeBeforeUpdate = publisherRepository.findAll().size

// Update the publisher using partial update
        val partialUpdatedPublisher = Publisher().apply {
            id = publisher.id
        }

        restPublisherMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedPublisher.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedPublisher))
        )
            .andExpect(status().isOk)

// Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
        val testPublisher = publisherList.last()
        assertThat(testPublisher.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdatePublisherWithPatch() {

        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        val databaseSizeBeforeUpdate = publisherRepository.findAll().size

// Update the publisher using partial update
        val partialUpdatedPublisher = Publisher().apply {
            id = publisher.id

            name = UPDATED_NAME
        }

        restPublisherMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedPublisher.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedPublisher))
        )
            .andExpect(status().isOk)

// Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
        val testPublisher = publisherList.last()
        assertThat(testPublisher.name).isEqualTo(UPDATED_NAME)
    }

    @Throws(Exception::class)
    fun patchNonExistingPublisher() {
        val databaseSizeBeforeUpdate = publisherRepository.findAll().size
        publisher.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPublisherMockMvc.perform(
            patch(ENTITY_API_URL_ID, publisher.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(publisher))
        )
            .andExpect(status().isBadRequest)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchPublisher() {
        val databaseSizeBeforeUpdate = publisherRepository.findAll().size
        publisher.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPublisherMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(publisher))
        )
            .andExpect(status().isBadRequest)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamPublisher() {
        val databaseSizeBeforeUpdate = publisherRepository.findAll().size
        publisher.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPublisherMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(publisher))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Publisher in the database
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deletePublisher() {
        // Initialize the database
        publisherRepository.saveAndFlush(publisher)

        val databaseSizeBeforeDelete = publisherRepository.findAll().size

        // Delete the publisher
        restPublisherMockMvc.perform(
            delete(ENTITY_API_URL_ID, publisher.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val publisherList = publisherRepository.findAll()
        assertThat(publisherList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private val ENTITY_API_URL: String = "/api/publishers"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + (2 * Integer.MAX_VALUE))

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Publisher {
            val publisher = Publisher(

                name = DEFAULT_NAME

            )

            return publisher
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Publisher {
            val publisher = Publisher(

                name = UPDATED_NAME

            )

            return publisher
        }
    }
}
