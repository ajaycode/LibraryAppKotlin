package com.mycompany.myapp.web.rest

import com.mycompany.myapp.IntegrationTest
import com.mycompany.myapp.domain.BorrowedBook
import com.mycompany.myapp.repository.BorrowedBookRepository
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
import java.time.LocalDate
import java.time.ZoneId
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [BorrowedBookResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BorrowedBookResourceIT {
    @Autowired
    private lateinit var borrowedBookRepository: BorrowedBookRepository

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
    private lateinit var restBorrowedBookMockMvc: MockMvc

    private lateinit var borrowedBook: BorrowedBook

    @BeforeEach
    fun initTest() {
        borrowedBook = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createBorrowedBook() {
        val databaseSizeBeforeCreate = borrowedBookRepository.findAll().size

        // Create the BorrowedBook
        restBorrowedBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(borrowedBook))
        ).andExpect(status().isCreated)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeCreate + 1)
        val testBorrowedBook = borrowedBookList[borrowedBookList.size - 1]

        assertThat(testBorrowedBook.borrowDate).isEqualTo(DEFAULT_BORROW_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createBorrowedBookWithExistingId() {
        // Create the BorrowedBook with an existing ID
        borrowedBook.id = 1L

        val databaseSizeBeforeCreate = borrowedBookRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restBorrowedBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(borrowedBook))
        ).andExpect(status().isBadRequest)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooks() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList
        restBorrowedBookMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(borrowedBook.id?.toInt())))
            .andExpect(jsonPath("$.[*].borrowDate").value(hasItem(DEFAULT_BORROW_DATE.toString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getBorrowedBook() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        val id = borrowedBook.id
        assertNotNull(id)

        // Get the borrowedBook
        restBorrowedBookMockMvc.perform(get(ENTITY_API_URL_ID, borrowedBook.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(borrowedBook.id?.toInt()))
            .andExpect(jsonPath("$.borrowDate").value(DEFAULT_BORROW_DATE.toString()))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getBorrowedBooksByIdFiltering() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)
        val id = borrowedBook.id

        defaultBorrowedBookShouldBeFound("id.equals=" + id)
        defaultBorrowedBookShouldNotBeFound("id.notEquals=" + id)
        defaultBorrowedBookShouldBeFound("id.greaterThanOrEqual=" + id)
        defaultBorrowedBookShouldNotBeFound("id.greaterThan=" + id)

        defaultBorrowedBookShouldBeFound("id.lessThanOrEqual=" + id)
        defaultBorrowedBookShouldNotBeFound("id.lessThan=" + id)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsEqualToSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate equals to DEFAULT_BORROW_DATE
        defaultBorrowedBookShouldBeFound("borrowDate.equals=$DEFAULT_BORROW_DATE")

        // Get all the borrowedBookList where borrowDate equals to UPDATED_BORROW_DATE
        defaultBorrowedBookShouldNotBeFound("borrowDate.equals=$UPDATED_BORROW_DATE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsNotEqualToSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate not equals to DEFAULT_BORROW_DATE
        defaultBorrowedBookShouldNotBeFound("borrowDate.notEquals=" + DEFAULT_BORROW_DATE)

        // Get all the borrowedBookList where borrowDate not equals to UPDATED_BORROW_DATE
        defaultBorrowedBookShouldBeFound("borrowDate.notEquals=" + UPDATED_BORROW_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsInShouldWork() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate in DEFAULT_BORROW_DATE or UPDATED_BORROW_DATE
        defaultBorrowedBookShouldBeFound("borrowDate.in=$DEFAULT_BORROW_DATE,$UPDATED_BORROW_DATE")

        // Get all the borrowedBookList where borrowDate equals to UPDATED_BORROW_DATE
        defaultBorrowedBookShouldNotBeFound("borrowDate.in=$UPDATED_BORROW_DATE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsNullOrNotNull() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate is not null
        defaultBorrowedBookShouldBeFound("borrowDate.specified=true")

        // Get all the borrowedBookList where borrowDate is null
        defaultBorrowedBookShouldNotBeFound("borrowDate.specified=false")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate is greater than or equal to DEFAULT_BORROW_DATE
        defaultBorrowedBookShouldBeFound("borrowDate.greaterThanOrEqual=$DEFAULT_BORROW_DATE")

        // Get all the borrowedBookList where borrowDate is greater than or equal to UPDATED_BORROW_DATE
        defaultBorrowedBookShouldNotBeFound("borrowDate.greaterThanOrEqual=$UPDATED_BORROW_DATE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsLessThanOrEqualToSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate is less than or equal to DEFAULT_BORROW_DATE
        defaultBorrowedBookShouldBeFound("borrowDate.lessThanOrEqual=$DEFAULT_BORROW_DATE")

        // Get all the borrowedBookList where borrowDate is less than or equal to SMALLER_BORROW_DATE
        defaultBorrowedBookShouldNotBeFound("borrowDate.lessThanOrEqual=$SMALLER_BORROW_DATE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsLessThanSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate is less than DEFAULT_BORROW_DATE
        defaultBorrowedBookShouldNotBeFound("borrowDate.lessThan=$DEFAULT_BORROW_DATE")

        // Get all the borrowedBookList where borrowDate is less than UPDATED_BORROW_DATE
        defaultBorrowedBookShouldBeFound("borrowDate.lessThan=$UPDATED_BORROW_DATE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBorrowDateIsGreaterThanSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        // Get all the borrowedBookList where borrowDate is greater than DEFAULT_BORROW_DATE
        defaultBorrowedBookShouldNotBeFound("borrowDate.greaterThan=$DEFAULT_BORROW_DATE")

        // Get all the borrowedBookList where borrowDate is greater than SMALLER_BORROW_DATE
        defaultBorrowedBookShouldBeFound("borrowDate.greaterThan=$SMALLER_BORROW_DATE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByBookIsEqualToSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)
        val book = BookResourceIT.createEntity(em)
        em.persist(book)
        em.flush()
        borrowedBook.book = book
        borrowedBookRepository.saveAndFlush(borrowedBook)
        val bookId = book.id

        // Get all the borrowedBookList where book equals to bookId
        defaultBorrowedBookShouldBeFound("bookId.equals=$bookId")

        // Get all the borrowedBookList where book equals to (bookId?.plus(1))
        defaultBorrowedBookShouldNotBeFound("bookId.equals=${(bookId?.plus(1))}")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBorrowedBooksByClientIsEqualToSomething() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)
        val client = ClientResourceIT.createEntity(em)
        em.persist(client)
        em.flush()
        borrowedBook.client = client
        borrowedBookRepository.saveAndFlush(borrowedBook)
        val clientId = client.id

        // Get all the borrowedBookList where client equals to clientId
        defaultBorrowedBookShouldBeFound("clientId.equals=$clientId")

        // Get all the borrowedBookList where client equals to (clientId?.plus(1))
        defaultBorrowedBookShouldNotBeFound("clientId.equals=${(clientId?.plus(1))}")
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */

    @Throws(Exception::class)
    private fun defaultBorrowedBookShouldBeFound(filter: String) {
        restBorrowedBookMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(borrowedBook.id?.toInt())))
            .andExpect(jsonPath("$.[*].borrowDate").value(hasItem(DEFAULT_BORROW_DATE.toString())))

        // Check, that the count call also returns 1
        restBorrowedBookMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"))
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    @Throws(Exception::class)
    private fun defaultBorrowedBookShouldNotBeFound(filter: String) {
        restBorrowedBookMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Check, that the count call also returns 0
        restBorrowedBookMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingBorrowedBook() {
        // Get the borrowedBook
        restBorrowedBookMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewBorrowedBook() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size

        // Update the borrowedBook
        val updatedBorrowedBook = borrowedBookRepository.findById(borrowedBook.id).get()
        // Disconnect from session so that the updates on updatedBorrowedBook are not directly saved in db
        em.detach(updatedBorrowedBook)
        updatedBorrowedBook.borrowDate = UPDATED_BORROW_DATE

        restBorrowedBookMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedBorrowedBook.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedBorrowedBook))
        ).andExpect(status().isOk)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
        val testBorrowedBook = borrowedBookList[borrowedBookList.size - 1]
        assertThat(testBorrowedBook.borrowDate).isEqualTo(UPDATED_BORROW_DATE)
    }

    @Test
    @Transactional
    fun putNonExistingBorrowedBook() {
        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size
        borrowedBook.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBorrowedBookMockMvc.perform(
            put(ENTITY_API_URL_ID, borrowedBook.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(borrowedBook))
        )
            .andExpect(status().isBadRequest)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchBorrowedBook() {
        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size
        borrowedBook.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBorrowedBookMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(borrowedBook))
        ).andExpect(status().isBadRequest)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamBorrowedBook() {
        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size
        borrowedBook.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBorrowedBookMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(borrowedBook))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateBorrowedBookWithPatch() {

        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size

// Update the borrowedBook using partial update
        val partialUpdatedBorrowedBook = BorrowedBook().apply {
            id = borrowedBook.id
        }

        restBorrowedBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedBorrowedBook.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedBorrowedBook))
        )
            .andExpect(status().isOk)

// Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
        val testBorrowedBook = borrowedBookList.last()
        assertThat(testBorrowedBook.borrowDate).isEqualTo(DEFAULT_BORROW_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateBorrowedBookWithPatch() {

        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size

// Update the borrowedBook using partial update
        val partialUpdatedBorrowedBook = BorrowedBook().apply {
            id = borrowedBook.id

            borrowDate = UPDATED_BORROW_DATE
        }

        restBorrowedBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedBorrowedBook.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedBorrowedBook))
        )
            .andExpect(status().isOk)

// Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
        val testBorrowedBook = borrowedBookList.last()
        assertThat(testBorrowedBook.borrowDate).isEqualTo(UPDATED_BORROW_DATE)
    }

    @Throws(Exception::class)
    fun patchNonExistingBorrowedBook() {
        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size
        borrowedBook.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBorrowedBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, borrowedBook.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(borrowedBook))
        )
            .andExpect(status().isBadRequest)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchBorrowedBook() {
        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size
        borrowedBook.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBorrowedBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(borrowedBook))
        )
            .andExpect(status().isBadRequest)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamBorrowedBook() {
        val databaseSizeBeforeUpdate = borrowedBookRepository.findAll().size
        borrowedBook.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBorrowedBookMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(borrowedBook))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the BorrowedBook in the database
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteBorrowedBook() {
        // Initialize the database
        borrowedBookRepository.saveAndFlush(borrowedBook)

        val databaseSizeBeforeDelete = borrowedBookRepository.findAll().size

        // Delete the borrowedBook
        restBorrowedBookMockMvc.perform(
            delete(ENTITY_API_URL_ID, borrowedBook.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val borrowedBookList = borrowedBookRepository.findAll()
        assertThat(borrowedBookList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private val DEFAULT_BORROW_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_BORROW_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())
        private val SMALLER_BORROW_DATE: LocalDate = LocalDate.ofEpochDay(-1L)

        private val ENTITY_API_URL: String = "/api/borrowed-books"
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
        fun createEntity(em: EntityManager): BorrowedBook {
            val borrowedBook = BorrowedBook(

                borrowDate = DEFAULT_BORROW_DATE

            )

            return borrowedBook
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): BorrowedBook {
            val borrowedBook = BorrowedBook(

                borrowDate = UPDATED_BORROW_DATE

            )

            return borrowedBook
        }
    }
}
