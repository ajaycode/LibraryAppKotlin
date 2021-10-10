package com.mycompany.myapp.web.rest

import com.mycompany.myapp.IntegrationTest
import com.mycompany.myapp.domain.Book
import com.mycompany.myapp.repository.BookRepository
import com.mycompany.myapp.service.BookService
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Base64Utils
import org.springframework.validation.Validator
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [BookResource] REST controller.
 */
@IntegrationTest
@Extensions(
    ExtendWith(MockitoExtension::class)
)
@AutoConfigureMockMvc
@WithMockUser
class BookResourceIT {
    @Autowired
    private lateinit var bookRepository: BookRepository

    @Mock
    private lateinit var bookRepositoryMock: BookRepository

    @Mock
    private lateinit var bookServiceMock: BookService

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
    private lateinit var restBookMockMvc: MockMvc

    private lateinit var book: Book

    @BeforeEach
    fun initTest() {
        book = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createBook() {
        val databaseSizeBeforeCreate = bookRepository.findAll().size

        // Create the Book
        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isCreated)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate + 1)
        val testBook = bookList[bookList.size - 1]

        assertThat(testBook.isbn).isEqualTo(DEFAULT_ISBN)
        assertThat(testBook.name).isEqualTo(DEFAULT_NAME)
        assertThat(testBook.publishYear).isEqualTo(DEFAULT_PUBLISH_YEAR)
        assertThat(testBook.copies).isEqualTo(DEFAULT_COPIES)
        assertThat(testBook.cover).isEqualTo(DEFAULT_COVER)
        assertThat(testBook.coverContentType).isEqualTo(DEFAULT_COVER_CONTENT_TYPE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createBookWithExistingId() {
        // Create the Book with an existing ID
        book.id = 1L

        val databaseSizeBeforeCreate = bookRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkIsbnIsRequired() {
        val databaseSizeBeforeTest = bookRepository.findAll().size
        // set the field null
        book.isbn = null

        // Create the Book, which fails.

        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = bookRepository.findAll().size
        // set the field null
        book.name = null

        // Create the Book, which fails.

        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkPublishYearIsRequired() {
        val databaseSizeBeforeTest = bookRepository.findAll().size
        // set the field null
        book.publishYear = null

        // Create the Book, which fails.

        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkCopiesIsRequired() {
        val databaseSizeBeforeTest = bookRepository.findAll().size
        // set the field null
        book.copies = null

        // Create the Book, which fails.

        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooks() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList
        restBookMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.id?.toInt())))
            .andExpect(jsonPath("$.[*].isbn").value(hasItem(DEFAULT_ISBN)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].publishYear").value(hasItem(DEFAULT_PUBLISH_YEAR)))
            .andExpect(jsonPath("$.[*].copies").value(hasItem(DEFAULT_COPIES)))
            .andExpect(jsonPath("$.[*].coverContentType").value(hasItem(DEFAULT_COVER_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(Base64Utils.encodeToString(DEFAULT_COVER))))
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllBooksWithEagerRelationshipsIsEnabled() {
        `when`(bookServiceMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restBookMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true"))
            .andExpect(status().isOk)

        verify(bookServiceMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllBooksWithEagerRelationshipsIsNotEnabled() {
        `when`(bookServiceMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restBookMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true"))
            .andExpect(status().isOk)

        verify(bookServiceMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val id = book.id
        assertNotNull(id)

        // Get the book
        restBookMockMvc.perform(get(ENTITY_API_URL_ID, book.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(book.id?.toInt()))
            .andExpect(jsonPath("$.isbn").value(DEFAULT_ISBN))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.publishYear").value(DEFAULT_PUBLISH_YEAR))
            .andExpect(jsonPath("$.copies").value(DEFAULT_COPIES))
            .andExpect(jsonPath("$.coverContentType").value(DEFAULT_COVER_CONTENT_TYPE))
            .andExpect(jsonPath("$.cover").value(Base64Utils.encodeToString(DEFAULT_COVER)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getBooksByIdFiltering() {
        // Initialize the database
        bookRepository.saveAndFlush(book)
        val id = book.id

        defaultBookShouldBeFound("id.equals=" + id)
        defaultBookShouldNotBeFound("id.notEquals=" + id)
        defaultBookShouldBeFound("id.greaterThanOrEqual=" + id)
        defaultBookShouldNotBeFound("id.greaterThan=" + id)

        defaultBookShouldBeFound("id.lessThanOrEqual=" + id)
        defaultBookShouldNotBeFound("id.lessThan=" + id)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByIsbnIsEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where isbn equals to DEFAULT_ISBN
        defaultBookShouldBeFound("isbn.equals=$DEFAULT_ISBN")

        // Get all the bookList where isbn equals to UPDATED_ISBN
        defaultBookShouldNotBeFound("isbn.equals=$UPDATED_ISBN")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByIsbnIsNotEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where isbn not equals to DEFAULT_ISBN
        defaultBookShouldNotBeFound("isbn.notEquals=" + DEFAULT_ISBN)

        // Get all the bookList where isbn not equals to UPDATED_ISBN
        defaultBookShouldBeFound("isbn.notEquals=" + UPDATED_ISBN)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByIsbnIsInShouldWork() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where isbn in DEFAULT_ISBN or UPDATED_ISBN
        defaultBookShouldBeFound("isbn.in=$DEFAULT_ISBN,$UPDATED_ISBN")

        // Get all the bookList where isbn equals to UPDATED_ISBN
        defaultBookShouldNotBeFound("isbn.in=$UPDATED_ISBN")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByIsbnIsNullOrNotNull() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where isbn is not null
        defaultBookShouldBeFound("isbn.specified=true")

        // Get all the bookList where isbn is null
        defaultBookShouldNotBeFound("isbn.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByIsbnContainsSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where isbn contains DEFAULT_ISBN
        defaultBookShouldBeFound("isbn.contains=" + DEFAULT_ISBN)

        // Get all the bookList where isbn contains UPDATED_ISBN
        defaultBookShouldNotBeFound("isbn.contains=" + UPDATED_ISBN)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByIsbnNotContainsSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where isbn does not contain DEFAULT_ISBN
        defaultBookShouldNotBeFound("isbn.doesNotContain=" + DEFAULT_ISBN)

        // Get all the bookList where isbn does not contain UPDATED_ISBN
        defaultBookShouldBeFound("isbn.doesNotContain=" + UPDATED_ISBN)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByNameIsEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where name equals to DEFAULT_NAME
        defaultBookShouldBeFound("name.equals=$DEFAULT_NAME")

        // Get all the bookList where name equals to UPDATED_NAME
        defaultBookShouldNotBeFound("name.equals=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByNameIsNotEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where name not equals to DEFAULT_NAME
        defaultBookShouldNotBeFound("name.notEquals=" + DEFAULT_NAME)

        // Get all the bookList where name not equals to UPDATED_NAME
        defaultBookShouldBeFound("name.notEquals=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByNameIsInShouldWork() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where name in DEFAULT_NAME or UPDATED_NAME
        defaultBookShouldBeFound("name.in=$DEFAULT_NAME,$UPDATED_NAME")

        // Get all the bookList where name equals to UPDATED_NAME
        defaultBookShouldNotBeFound("name.in=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByNameIsNullOrNotNull() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where name is not null
        defaultBookShouldBeFound("name.specified=true")

        // Get all the bookList where name is null
        defaultBookShouldNotBeFound("name.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByNameContainsSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where name contains DEFAULT_NAME
        defaultBookShouldBeFound("name.contains=" + DEFAULT_NAME)

        // Get all the bookList where name contains UPDATED_NAME
        defaultBookShouldNotBeFound("name.contains=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByNameNotContainsSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where name does not contain DEFAULT_NAME
        defaultBookShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME)

        // Get all the bookList where name does not contain UPDATED_NAME
        defaultBookShouldBeFound("name.doesNotContain=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByPublishYearIsEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where publishYear equals to DEFAULT_PUBLISH_YEAR
        defaultBookShouldBeFound("publishYear.equals=$DEFAULT_PUBLISH_YEAR")

        // Get all the bookList where publishYear equals to UPDATED_PUBLISH_YEAR
        defaultBookShouldNotBeFound("publishYear.equals=$UPDATED_PUBLISH_YEAR")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByPublishYearIsNotEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where publishYear not equals to DEFAULT_PUBLISH_YEAR
        defaultBookShouldNotBeFound("publishYear.notEquals=" + DEFAULT_PUBLISH_YEAR)

        // Get all the bookList where publishYear not equals to UPDATED_PUBLISH_YEAR
        defaultBookShouldBeFound("publishYear.notEquals=" + UPDATED_PUBLISH_YEAR)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByPublishYearIsInShouldWork() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where publishYear in DEFAULT_PUBLISH_YEAR or UPDATED_PUBLISH_YEAR
        defaultBookShouldBeFound("publishYear.in=$DEFAULT_PUBLISH_YEAR,$UPDATED_PUBLISH_YEAR")

        // Get all the bookList where publishYear equals to UPDATED_PUBLISH_YEAR
        defaultBookShouldNotBeFound("publishYear.in=$UPDATED_PUBLISH_YEAR")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByPublishYearIsNullOrNotNull() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where publishYear is not null
        defaultBookShouldBeFound("publishYear.specified=true")

        // Get all the bookList where publishYear is null
        defaultBookShouldNotBeFound("publishYear.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByPublishYearContainsSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where publishYear contains DEFAULT_PUBLISH_YEAR
        defaultBookShouldBeFound("publishYear.contains=" + DEFAULT_PUBLISH_YEAR)

        // Get all the bookList where publishYear contains UPDATED_PUBLISH_YEAR
        defaultBookShouldNotBeFound("publishYear.contains=" + UPDATED_PUBLISH_YEAR)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByPublishYearNotContainsSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where publishYear does not contain DEFAULT_PUBLISH_YEAR
        defaultBookShouldNotBeFound("publishYear.doesNotContain=" + DEFAULT_PUBLISH_YEAR)

        // Get all the bookList where publishYear does not contain UPDATED_PUBLISH_YEAR
        defaultBookShouldBeFound("publishYear.doesNotContain=" + UPDATED_PUBLISH_YEAR)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies equals to DEFAULT_COPIES
        defaultBookShouldBeFound("copies.equals=$DEFAULT_COPIES")

        // Get all the bookList where copies equals to UPDATED_COPIES
        defaultBookShouldNotBeFound("copies.equals=$UPDATED_COPIES")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsNotEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies not equals to DEFAULT_COPIES
        defaultBookShouldNotBeFound("copies.notEquals=" + DEFAULT_COPIES)

        // Get all the bookList where copies not equals to UPDATED_COPIES
        defaultBookShouldBeFound("copies.notEquals=" + UPDATED_COPIES)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsInShouldWork() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies in DEFAULT_COPIES or UPDATED_COPIES
        defaultBookShouldBeFound("copies.in=$DEFAULT_COPIES,$UPDATED_COPIES")

        // Get all the bookList where copies equals to UPDATED_COPIES
        defaultBookShouldNotBeFound("copies.in=$UPDATED_COPIES")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsNullOrNotNull() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies is not null
        defaultBookShouldBeFound("copies.specified=true")

        // Get all the bookList where copies is null
        defaultBookShouldNotBeFound("copies.specified=false")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies is greater than or equal to DEFAULT_COPIES
        defaultBookShouldBeFound("copies.greaterThanOrEqual=$DEFAULT_COPIES")

        // Get all the bookList where copies is greater than or equal to UPDATED_COPIES
        defaultBookShouldNotBeFound("copies.greaterThanOrEqual=$UPDATED_COPIES")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsLessThanOrEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies is less than or equal to DEFAULT_COPIES
        defaultBookShouldBeFound("copies.lessThanOrEqual=$DEFAULT_COPIES")

        // Get all the bookList where copies is less than or equal to SMALLER_COPIES
        defaultBookShouldNotBeFound("copies.lessThanOrEqual=$SMALLER_COPIES")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsLessThanSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies is less than DEFAULT_COPIES
        defaultBookShouldNotBeFound("copies.lessThan=$DEFAULT_COPIES")

        // Get all the bookList where copies is less than UPDATED_COPIES
        defaultBookShouldBeFound("copies.lessThan=$UPDATED_COPIES")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByCopiesIsGreaterThanSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList where copies is greater than DEFAULT_COPIES
        defaultBookShouldNotBeFound("copies.greaterThan=$DEFAULT_COPIES")

        // Get all the bookList where copies is greater than SMALLER_COPIES
        defaultBookShouldBeFound("copies.greaterThan=$SMALLER_COPIES")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByPublisherIsEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)
        val publisher = PublisherResourceIT.createEntity(em)
        em.persist(publisher)
        em.flush()
        book.publisher = publisher
        bookRepository.saveAndFlush(book)
        val publisherId = publisher.id

        // Get all the bookList where publisher equals to publisherId
        defaultBookShouldBeFound("publisherId.equals=$publisherId")

        // Get all the bookList where publisher equals to (publisherId?.plus(1))
        defaultBookShouldNotBeFound("publisherId.equals=${(publisherId?.plus(1))}")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooksByAuthorIsEqualToSomething() {
        // Initialize the database
        bookRepository.saveAndFlush(book)
        val author = AuthorResourceIT.createEntity(em)
        em.persist(author)
        em.flush()
        book.addAuthor(author)
        bookRepository.saveAndFlush(book)
        val authorId = author.id

        // Get all the bookList where author equals to authorId
        defaultBookShouldBeFound("authorId.equals=$authorId")

        // Get all the bookList where author equals to (authorId?.plus(1))
        defaultBookShouldNotBeFound("authorId.equals=${(authorId?.plus(1))}")
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */

    @Throws(Exception::class)
    private fun defaultBookShouldBeFound(filter: String) {
        restBookMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.id?.toInt())))
            .andExpect(jsonPath("$.[*].isbn").value(hasItem(DEFAULT_ISBN)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].publishYear").value(hasItem(DEFAULT_PUBLISH_YEAR)))
            .andExpect(jsonPath("$.[*].copies").value(hasItem(DEFAULT_COPIES)))
            .andExpect(jsonPath("$.[*].coverContentType").value(hasItem(DEFAULT_COVER_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(Base64Utils.encodeToString(DEFAULT_COVER))))

        // Check, that the count call also returns 1
        restBookMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"))
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    @Throws(Exception::class)
    private fun defaultBookShouldNotBeFound(filter: String) {
        restBookMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Check, that the count call also returns 0
        restBookMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingBook() {
        // Get the book
        restBookMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeUpdate = bookRepository.findAll().size

        // Update the book
        val updatedBook = bookRepository.findById(book.id).get()
        // Disconnect from session so that the updates on updatedBook are not directly saved in db
        em.detach(updatedBook)
        updatedBook.isbn = UPDATED_ISBN
        updatedBook.name = UPDATED_NAME
        updatedBook.publishYear = UPDATED_PUBLISH_YEAR
        updatedBook.copies = UPDATED_COPIES
        updatedBook.cover = UPDATED_COVER
        updatedBook.coverContentType = UPDATED_COVER_CONTENT_TYPE

        restBookMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedBook.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedBook))
        ).andExpect(status().isOk)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList[bookList.size - 1]
        assertThat(testBook.isbn).isEqualTo(UPDATED_ISBN)
        assertThat(testBook.name).isEqualTo(UPDATED_NAME)
        assertThat(testBook.publishYear).isEqualTo(UPDATED_PUBLISH_YEAR)
        assertThat(testBook.copies).isEqualTo(UPDATED_COPIES)
        assertThat(testBook.cover).isEqualTo(UPDATED_COVER)
        assertThat(testBook.coverContentType).isEqualTo(UPDATED_COVER_CONTENT_TYPE)
    }

    @Test
    @Transactional
    fun putNonExistingBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            put(ENTITY_API_URL_ID, book.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateBookWithPatch() {

        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeUpdate = bookRepository.findAll().size

// Update the book using partial update
        val partialUpdatedBook = Book().apply {
            id = book.id

            isbn = UPDATED_ISBN
            name = UPDATED_NAME
            publishYear = UPDATED_PUBLISH_YEAR
            copies = UPDATED_COPIES
            cover = UPDATED_COVER
            coverContentType = UPDATED_COVER_CONTENT_TYPE
        }

        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedBook.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedBook))
        )
            .andExpect(status().isOk)

// Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList.last()
        assertThat(testBook.isbn).isEqualTo(UPDATED_ISBN)
        assertThat(testBook.name).isEqualTo(UPDATED_NAME)
        assertThat(testBook.publishYear).isEqualTo(UPDATED_PUBLISH_YEAR)
        assertThat(testBook.copies).isEqualTo(UPDATED_COPIES)
        assertThat(testBook.cover).isEqualTo(UPDATED_COVER)
        assertThat(testBook.coverContentType).isEqualTo(UPDATED_COVER_CONTENT_TYPE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateBookWithPatch() {

        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeUpdate = bookRepository.findAll().size

// Update the book using partial update
        val partialUpdatedBook = Book().apply {
            id = book.id

            isbn = UPDATED_ISBN
            name = UPDATED_NAME
            publishYear = UPDATED_PUBLISH_YEAR
            copies = UPDATED_COPIES
            cover = UPDATED_COVER
            coverContentType = UPDATED_COVER_CONTENT_TYPE
        }

        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedBook.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedBook))
        )
            .andExpect(status().isOk)

// Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList.last()
        assertThat(testBook.isbn).isEqualTo(UPDATED_ISBN)
        assertThat(testBook.name).isEqualTo(UPDATED_NAME)
        assertThat(testBook.publishYear).isEqualTo(UPDATED_PUBLISH_YEAR)
        assertThat(testBook.copies).isEqualTo(UPDATED_COPIES)
        assertThat(testBook.cover).isEqualTo(UPDATED_COVER)
        assertThat(testBook.coverContentType).isEqualTo(UPDATED_COVER_CONTENT_TYPE)
    }

    @Throws(Exception::class)
    fun patchNonExistingBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, book.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeDelete = bookRepository.findAll().size

        // Delete the book
        restBookMockMvc.perform(
            delete(ENTITY_API_URL_ID, book.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_ISBN = "AAAAAAAAAA"
        private const val UPDATED_ISBN = "BBBBBBBBBB"

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_PUBLISH_YEAR = "AAAAAAAAAA"
        private const val UPDATED_PUBLISH_YEAR = "BBBBBBBBBB"

        private const val DEFAULT_COPIES: Int = 1
        private const val UPDATED_COPIES: Int = 2
        private const val SMALLER_COPIES: Int = 1 - 1

        private val DEFAULT_COVER: ByteArray = createByteArray(1, "0")
        private val UPDATED_COVER: ByteArray = createByteArray(1, "1")
        private const val DEFAULT_COVER_CONTENT_TYPE: String = "image/jpg"
        private const val UPDATED_COVER_CONTENT_TYPE: String = "image/png"

        private val ENTITY_API_URL: String = "/api/books"
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
        fun createEntity(em: EntityManager): Book {
            val book = Book(

                isbn = DEFAULT_ISBN,

                name = DEFAULT_NAME,

                publishYear = DEFAULT_PUBLISH_YEAR,

                copies = DEFAULT_COPIES,

                cover = DEFAULT_COVER,
                coverContentType = DEFAULT_COVER_CONTENT_TYPE

            )

            return book
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Book {
            val book = Book(

                isbn = UPDATED_ISBN,

                name = UPDATED_NAME,

                publishYear = UPDATED_PUBLISH_YEAR,

                copies = UPDATED_COPIES,

                cover = UPDATED_COVER,
                coverContentType = UPDATED_COVER_CONTENT_TYPE

            )

            return book
        }
    }
}
