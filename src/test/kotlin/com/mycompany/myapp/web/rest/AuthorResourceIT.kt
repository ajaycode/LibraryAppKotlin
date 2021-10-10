package com.mycompany.myapp.web.rest

import com.mycompany.myapp.IntegrationTest
import com.mycompany.myapp.domain.Author
import com.mycompany.myapp.repository.AuthorRepository
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
 * Integration tests for the [AuthorResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AuthorResourceIT {
    @Autowired
    private lateinit var authorRepository: AuthorRepository

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
    private lateinit var restAuthorMockMvc: MockMvc

    private lateinit var author: Author

    @BeforeEach
    fun initTest() {
        author = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createAuthor() {
        val databaseSizeBeforeCreate = authorRepository.findAll().size

        // Create the Author
        restAuthorMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isCreated)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeCreate + 1)
        val testAuthor = authorList[authorList.size - 1]

        assertThat(testAuthor.firstName).isEqualTo(DEFAULT_FIRST_NAME)
        assertThat(testAuthor.lastName).isEqualTo(DEFAULT_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createAuthorWithExistingId() {
        // Create the Author with an existing ID
        author.id = 1L

        val databaseSizeBeforeCreate = authorRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuthorMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isBadRequest)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkFirstNameIsRequired() {
        val databaseSizeBeforeTest = authorRepository.findAll().size
        // set the field null
        author.firstName = null

        // Create the Author, which fails.

        restAuthorMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isBadRequest)

        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkLastNameIsRequired() {
        val databaseSizeBeforeTest = authorRepository.findAll().size
        // set the field null
        author.lastName = null

        // Create the Author, which fails.

        restAuthorMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isBadRequest)

        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthors() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList
        restAuthorMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(author.id?.toInt())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAuthor() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        val id = author.id
        assertNotNull(id)

        // Get the author
        restAuthorMockMvc.perform(get(ENTITY_API_URL_ID, author.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(author.id?.toInt()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAuthorsByIdFiltering() {
        // Initialize the database
        authorRepository.saveAndFlush(author)
        val id = author.id

        defaultAuthorShouldBeFound("id.equals=" + id)
        defaultAuthorShouldNotBeFound("id.notEquals=" + id)
        defaultAuthorShouldBeFound("id.greaterThanOrEqual=" + id)
        defaultAuthorShouldNotBeFound("id.greaterThan=" + id)

        defaultAuthorShouldBeFound("id.lessThanOrEqual=" + id)
        defaultAuthorShouldNotBeFound("id.lessThan=" + id)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByFirstNameIsEqualToSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where firstName equals to DEFAULT_FIRST_NAME
        defaultAuthorShouldBeFound("firstName.equals=$DEFAULT_FIRST_NAME")

        // Get all the authorList where firstName equals to UPDATED_FIRST_NAME
        defaultAuthorShouldNotBeFound("firstName.equals=$UPDATED_FIRST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByFirstNameIsNotEqualToSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where firstName not equals to DEFAULT_FIRST_NAME
        defaultAuthorShouldNotBeFound("firstName.notEquals=" + DEFAULT_FIRST_NAME)

        // Get all the authorList where firstName not equals to UPDATED_FIRST_NAME
        defaultAuthorShouldBeFound("firstName.notEquals=" + UPDATED_FIRST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByFirstNameIsInShouldWork() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where firstName in DEFAULT_FIRST_NAME or UPDATED_FIRST_NAME
        defaultAuthorShouldBeFound("firstName.in=$DEFAULT_FIRST_NAME,$UPDATED_FIRST_NAME")

        // Get all the authorList where firstName equals to UPDATED_FIRST_NAME
        defaultAuthorShouldNotBeFound("firstName.in=$UPDATED_FIRST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByFirstNameIsNullOrNotNull() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where firstName is not null
        defaultAuthorShouldBeFound("firstName.specified=true")

        // Get all the authorList where firstName is null
        defaultAuthorShouldNotBeFound("firstName.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByFirstNameContainsSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where firstName contains DEFAULT_FIRST_NAME
        defaultAuthorShouldBeFound("firstName.contains=" + DEFAULT_FIRST_NAME)

        // Get all the authorList where firstName contains UPDATED_FIRST_NAME
        defaultAuthorShouldNotBeFound("firstName.contains=" + UPDATED_FIRST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByFirstNameNotContainsSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where firstName does not contain DEFAULT_FIRST_NAME
        defaultAuthorShouldNotBeFound("firstName.doesNotContain=" + DEFAULT_FIRST_NAME)

        // Get all the authorList where firstName does not contain UPDATED_FIRST_NAME
        defaultAuthorShouldBeFound("firstName.doesNotContain=" + UPDATED_FIRST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByLastNameIsEqualToSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where lastName equals to DEFAULT_LAST_NAME
        defaultAuthorShouldBeFound("lastName.equals=$DEFAULT_LAST_NAME")

        // Get all the authorList where lastName equals to UPDATED_LAST_NAME
        defaultAuthorShouldNotBeFound("lastName.equals=$UPDATED_LAST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByLastNameIsNotEqualToSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where lastName not equals to DEFAULT_LAST_NAME
        defaultAuthorShouldNotBeFound("lastName.notEquals=" + DEFAULT_LAST_NAME)

        // Get all the authorList where lastName not equals to UPDATED_LAST_NAME
        defaultAuthorShouldBeFound("lastName.notEquals=" + UPDATED_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByLastNameIsInShouldWork() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where lastName in DEFAULT_LAST_NAME or UPDATED_LAST_NAME
        defaultAuthorShouldBeFound("lastName.in=$DEFAULT_LAST_NAME,$UPDATED_LAST_NAME")

        // Get all the authorList where lastName equals to UPDATED_LAST_NAME
        defaultAuthorShouldNotBeFound("lastName.in=$UPDATED_LAST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByLastNameIsNullOrNotNull() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where lastName is not null
        defaultAuthorShouldBeFound("lastName.specified=true")

        // Get all the authorList where lastName is null
        defaultAuthorShouldNotBeFound("lastName.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByLastNameContainsSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where lastName contains DEFAULT_LAST_NAME
        defaultAuthorShouldBeFound("lastName.contains=" + DEFAULT_LAST_NAME)

        // Get all the authorList where lastName contains UPDATED_LAST_NAME
        defaultAuthorShouldNotBeFound("lastName.contains=" + UPDATED_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByLastNameNotContainsSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList where lastName does not contain DEFAULT_LAST_NAME
        defaultAuthorShouldNotBeFound("lastName.doesNotContain=" + DEFAULT_LAST_NAME)

        // Get all the authorList where lastName does not contain UPDATED_LAST_NAME
        defaultAuthorShouldBeFound("lastName.doesNotContain=" + UPDATED_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthorsByBookIsEqualToSomething() {
        // Initialize the database
        authorRepository.saveAndFlush(author)
        val book = BookResourceIT.createEntity(em)
        em.persist(book)
        em.flush()
        author.addBook(book)
        authorRepository.saveAndFlush(author)
        val bookId = book.id

        // Get all the authorList where book equals to bookId
        defaultAuthorShouldBeFound("bookId.equals=$bookId")

        // Get all the authorList where book equals to (bookId?.plus(1))
        defaultAuthorShouldNotBeFound("bookId.equals=${(bookId?.plus(1))}")
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */

    @Throws(Exception::class)
    private fun defaultAuthorShouldBeFound(filter: String) {
        restAuthorMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(author.id?.toInt())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))

        // Check, that the count call also returns 1
        restAuthorMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"))
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    @Throws(Exception::class)
    private fun defaultAuthorShouldNotBeFound(filter: String) {
        restAuthorMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Check, that the count call also returns 0
        restAuthorMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingAuthor() {
        // Get the author
        restAuthorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewAuthor() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        val databaseSizeBeforeUpdate = authorRepository.findAll().size

        // Update the author
        val updatedAuthor = authorRepository.findById(author.id).get()
        // Disconnect from session so that the updates on updatedAuthor are not directly saved in db
        em.detach(updatedAuthor)
        updatedAuthor.firstName = UPDATED_FIRST_NAME
        updatedAuthor.lastName = UPDATED_LAST_NAME

        restAuthorMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedAuthor.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedAuthor))
        ).andExpect(status().isOk)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
        val testAuthor = authorList[authorList.size - 1]
        assertThat(testAuthor.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testAuthor.lastName).isEqualTo(UPDATED_LAST_NAME)
    }

    @Test
    @Transactional
    fun putNonExistingAuthor() {
        val databaseSizeBeforeUpdate = authorRepository.findAll().size
        author.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorMockMvc.perform(
            put(ENTITY_API_URL_ID, author.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        )
            .andExpect(status().isBadRequest)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchAuthor() {
        val databaseSizeBeforeUpdate = authorRepository.findAll().size
        author.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isBadRequest)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamAuthor() {
        val databaseSizeBeforeUpdate = authorRepository.findAll().size
        author.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateAuthorWithPatch() {

        // Initialize the database
        authorRepository.saveAndFlush(author)

        val databaseSizeBeforeUpdate = authorRepository.findAll().size

// Update the author using partial update
        val partialUpdatedAuthor = Author().apply {
            id = author.id
        }

        restAuthorMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedAuthor.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedAuthor))
        )
            .andExpect(status().isOk)

// Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
        val testAuthor = authorList.last()
        assertThat(testAuthor.firstName).isEqualTo(DEFAULT_FIRST_NAME)
        assertThat(testAuthor.lastName).isEqualTo(DEFAULT_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateAuthorWithPatch() {

        // Initialize the database
        authorRepository.saveAndFlush(author)

        val databaseSizeBeforeUpdate = authorRepository.findAll().size

// Update the author using partial update
        val partialUpdatedAuthor = Author().apply {
            id = author.id

            firstName = UPDATED_FIRST_NAME
            lastName = UPDATED_LAST_NAME
        }

        restAuthorMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedAuthor.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedAuthor))
        )
            .andExpect(status().isOk)

// Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
        val testAuthor = authorList.last()
        assertThat(testAuthor.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testAuthor.lastName).isEqualTo(UPDATED_LAST_NAME)
    }

    @Throws(Exception::class)
    fun patchNonExistingAuthor() {
        val databaseSizeBeforeUpdate = authorRepository.findAll().size
        author.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorMockMvc.perform(
            patch(ENTITY_API_URL_ID, author.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(author))
        )
            .andExpect(status().isBadRequest)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchAuthor() {
        val databaseSizeBeforeUpdate = authorRepository.findAll().size
        author.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(author))
        )
            .andExpect(status().isBadRequest)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamAuthor() {
        val databaseSizeBeforeUpdate = authorRepository.findAll().size
        author.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(author))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteAuthor() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        val databaseSizeBeforeDelete = authorRepository.findAll().size

        // Delete the author
        restAuthorMockMvc.perform(
            delete(ENTITY_API_URL_ID, author.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_FIRST_NAME = "AAAAAAAAAA"
        private const val UPDATED_FIRST_NAME = "BBBBBBBBBB"

        private const val DEFAULT_LAST_NAME = "AAAAAAAAAA"
        private const val UPDATED_LAST_NAME = "BBBBBBBBBB"

        private val ENTITY_API_URL: String = "/api/authors"
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
        fun createEntity(em: EntityManager): Author {
            val author = Author(

                firstName = DEFAULT_FIRST_NAME,

                lastName = DEFAULT_LAST_NAME

            )

            return author
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Author {
            val author = Author(

                firstName = UPDATED_FIRST_NAME,

                lastName = UPDATED_LAST_NAME

            )

            return author
        }
    }
}
