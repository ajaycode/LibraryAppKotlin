package com.mycompany.myapp.web.rest

import com.mycompany.myapp.IntegrationTest
import com.mycompany.myapp.domain.Client
import com.mycompany.myapp.repository.ClientRepository
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
 * Integration tests for the [ClientResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ClientResourceIT {
    @Autowired
    private lateinit var clientRepository: ClientRepository

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
    private lateinit var restClientMockMvc: MockMvc

    private lateinit var client: Client

    @BeforeEach
    fun initTest() {
        client = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createClient() {
        val databaseSizeBeforeCreate = clientRepository.findAll().size

        // Create the Client
        restClientMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(client))
        ).andExpect(status().isCreated)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeCreate + 1)
        val testClient = clientList[clientList.size - 1]

        assertThat(testClient.firstName).isEqualTo(DEFAULT_FIRST_NAME)
        assertThat(testClient.lastName).isEqualTo(DEFAULT_LAST_NAME)
        assertThat(testClient.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(testClient.address).isEqualTo(DEFAULT_ADDRESS)
        assertThat(testClient.phone).isEqualTo(DEFAULT_PHONE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createClientWithExistingId() {
        // Create the Client with an existing ID
        client.id = 1L

        val databaseSizeBeforeCreate = clientRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restClientMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(client))
        ).andExpect(status().isBadRequest)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkFirstNameIsRequired() {
        val databaseSizeBeforeTest = clientRepository.findAll().size
        // set the field null
        client.firstName = null

        // Create the Client, which fails.

        restClientMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(client))
        ).andExpect(status().isBadRequest)

        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkLastNameIsRequired() {
        val databaseSizeBeforeTest = clientRepository.findAll().size
        // set the field null
        client.lastName = null

        // Create the Client, which fails.

        restClientMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(client))
        ).andExpect(status().isBadRequest)

        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClients() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList
        restClientMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(client.id?.toInt())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getClient() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        val id = client.id
        assertNotNull(id)

        // Get the client
        restClientMockMvc.perform(get(ENTITY_API_URL_ID, client.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(client.id?.toInt()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getClientsByIdFiltering() {
        // Initialize the database
        clientRepository.saveAndFlush(client)
        val id = client.id

        defaultClientShouldBeFound("id.equals=" + id)
        defaultClientShouldNotBeFound("id.notEquals=" + id)
        defaultClientShouldBeFound("id.greaterThanOrEqual=" + id)
        defaultClientShouldNotBeFound("id.greaterThan=" + id)

        defaultClientShouldBeFound("id.lessThanOrEqual=" + id)
        defaultClientShouldNotBeFound("id.lessThan=" + id)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByFirstNameIsEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where firstName equals to DEFAULT_FIRST_NAME
        defaultClientShouldBeFound("firstName.equals=$DEFAULT_FIRST_NAME")

        // Get all the clientList where firstName equals to UPDATED_FIRST_NAME
        defaultClientShouldNotBeFound("firstName.equals=$UPDATED_FIRST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByFirstNameIsNotEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where firstName not equals to DEFAULT_FIRST_NAME
        defaultClientShouldNotBeFound("firstName.notEquals=" + DEFAULT_FIRST_NAME)

        // Get all the clientList where firstName not equals to UPDATED_FIRST_NAME
        defaultClientShouldBeFound("firstName.notEquals=" + UPDATED_FIRST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByFirstNameIsInShouldWork() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where firstName in DEFAULT_FIRST_NAME or UPDATED_FIRST_NAME
        defaultClientShouldBeFound("firstName.in=$DEFAULT_FIRST_NAME,$UPDATED_FIRST_NAME")

        // Get all the clientList where firstName equals to UPDATED_FIRST_NAME
        defaultClientShouldNotBeFound("firstName.in=$UPDATED_FIRST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByFirstNameIsNullOrNotNull() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where firstName is not null
        defaultClientShouldBeFound("firstName.specified=true")

        // Get all the clientList where firstName is null
        defaultClientShouldNotBeFound("firstName.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByFirstNameContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where firstName contains DEFAULT_FIRST_NAME
        defaultClientShouldBeFound("firstName.contains=" + DEFAULT_FIRST_NAME)

        // Get all the clientList where firstName contains UPDATED_FIRST_NAME
        defaultClientShouldNotBeFound("firstName.contains=" + UPDATED_FIRST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByFirstNameNotContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where firstName does not contain DEFAULT_FIRST_NAME
        defaultClientShouldNotBeFound("firstName.doesNotContain=" + DEFAULT_FIRST_NAME)

        // Get all the clientList where firstName does not contain UPDATED_FIRST_NAME
        defaultClientShouldBeFound("firstName.doesNotContain=" + UPDATED_FIRST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByLastNameIsEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where lastName equals to DEFAULT_LAST_NAME
        defaultClientShouldBeFound("lastName.equals=$DEFAULT_LAST_NAME")

        // Get all the clientList where lastName equals to UPDATED_LAST_NAME
        defaultClientShouldNotBeFound("lastName.equals=$UPDATED_LAST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByLastNameIsNotEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where lastName not equals to DEFAULT_LAST_NAME
        defaultClientShouldNotBeFound("lastName.notEquals=" + DEFAULT_LAST_NAME)

        // Get all the clientList where lastName not equals to UPDATED_LAST_NAME
        defaultClientShouldBeFound("lastName.notEquals=" + UPDATED_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByLastNameIsInShouldWork() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where lastName in DEFAULT_LAST_NAME or UPDATED_LAST_NAME
        defaultClientShouldBeFound("lastName.in=$DEFAULT_LAST_NAME,$UPDATED_LAST_NAME")

        // Get all the clientList where lastName equals to UPDATED_LAST_NAME
        defaultClientShouldNotBeFound("lastName.in=$UPDATED_LAST_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByLastNameIsNullOrNotNull() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where lastName is not null
        defaultClientShouldBeFound("lastName.specified=true")

        // Get all the clientList where lastName is null
        defaultClientShouldNotBeFound("lastName.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByLastNameContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where lastName contains DEFAULT_LAST_NAME
        defaultClientShouldBeFound("lastName.contains=" + DEFAULT_LAST_NAME)

        // Get all the clientList where lastName contains UPDATED_LAST_NAME
        defaultClientShouldNotBeFound("lastName.contains=" + UPDATED_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByLastNameNotContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where lastName does not contain DEFAULT_LAST_NAME
        defaultClientShouldNotBeFound("lastName.doesNotContain=" + DEFAULT_LAST_NAME)

        // Get all the clientList where lastName does not contain UPDATED_LAST_NAME
        defaultClientShouldBeFound("lastName.doesNotContain=" + UPDATED_LAST_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByEmailIsEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where email equals to DEFAULT_EMAIL
        defaultClientShouldBeFound("email.equals=$DEFAULT_EMAIL")

        // Get all the clientList where email equals to UPDATED_EMAIL
        defaultClientShouldNotBeFound("email.equals=$UPDATED_EMAIL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByEmailIsNotEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where email not equals to DEFAULT_EMAIL
        defaultClientShouldNotBeFound("email.notEquals=" + DEFAULT_EMAIL)

        // Get all the clientList where email not equals to UPDATED_EMAIL
        defaultClientShouldBeFound("email.notEquals=" + UPDATED_EMAIL)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByEmailIsInShouldWork() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where email in DEFAULT_EMAIL or UPDATED_EMAIL
        defaultClientShouldBeFound("email.in=$DEFAULT_EMAIL,$UPDATED_EMAIL")

        // Get all the clientList where email equals to UPDATED_EMAIL
        defaultClientShouldNotBeFound("email.in=$UPDATED_EMAIL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByEmailIsNullOrNotNull() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where email is not null
        defaultClientShouldBeFound("email.specified=true")

        // Get all the clientList where email is null
        defaultClientShouldNotBeFound("email.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByEmailContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where email contains DEFAULT_EMAIL
        defaultClientShouldBeFound("email.contains=" + DEFAULT_EMAIL)

        // Get all the clientList where email contains UPDATED_EMAIL
        defaultClientShouldNotBeFound("email.contains=" + UPDATED_EMAIL)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByEmailNotContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where email does not contain DEFAULT_EMAIL
        defaultClientShouldNotBeFound("email.doesNotContain=" + DEFAULT_EMAIL)

        // Get all the clientList where email does not contain UPDATED_EMAIL
        defaultClientShouldBeFound("email.doesNotContain=" + UPDATED_EMAIL)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByAddressIsEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where address equals to DEFAULT_ADDRESS
        defaultClientShouldBeFound("address.equals=$DEFAULT_ADDRESS")

        // Get all the clientList where address equals to UPDATED_ADDRESS
        defaultClientShouldNotBeFound("address.equals=$UPDATED_ADDRESS")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByAddressIsNotEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where address not equals to DEFAULT_ADDRESS
        defaultClientShouldNotBeFound("address.notEquals=" + DEFAULT_ADDRESS)

        // Get all the clientList where address not equals to UPDATED_ADDRESS
        defaultClientShouldBeFound("address.notEquals=" + UPDATED_ADDRESS)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByAddressIsInShouldWork() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where address in DEFAULT_ADDRESS or UPDATED_ADDRESS
        defaultClientShouldBeFound("address.in=$DEFAULT_ADDRESS,$UPDATED_ADDRESS")

        // Get all the clientList where address equals to UPDATED_ADDRESS
        defaultClientShouldNotBeFound("address.in=$UPDATED_ADDRESS")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByAddressIsNullOrNotNull() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where address is not null
        defaultClientShouldBeFound("address.specified=true")

        // Get all the clientList where address is null
        defaultClientShouldNotBeFound("address.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByAddressContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where address contains DEFAULT_ADDRESS
        defaultClientShouldBeFound("address.contains=" + DEFAULT_ADDRESS)

        // Get all the clientList where address contains UPDATED_ADDRESS
        defaultClientShouldNotBeFound("address.contains=" + UPDATED_ADDRESS)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByAddressNotContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where address does not contain DEFAULT_ADDRESS
        defaultClientShouldNotBeFound("address.doesNotContain=" + DEFAULT_ADDRESS)

        // Get all the clientList where address does not contain UPDATED_ADDRESS
        defaultClientShouldBeFound("address.doesNotContain=" + UPDATED_ADDRESS)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByPhoneIsEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where phone equals to DEFAULT_PHONE
        defaultClientShouldBeFound("phone.equals=$DEFAULT_PHONE")

        // Get all the clientList where phone equals to UPDATED_PHONE
        defaultClientShouldNotBeFound("phone.equals=$UPDATED_PHONE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByPhoneIsNotEqualToSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where phone not equals to DEFAULT_PHONE
        defaultClientShouldNotBeFound("phone.notEquals=" + DEFAULT_PHONE)

        // Get all the clientList where phone not equals to UPDATED_PHONE
        defaultClientShouldBeFound("phone.notEquals=" + UPDATED_PHONE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByPhoneIsInShouldWork() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where phone in DEFAULT_PHONE or UPDATED_PHONE
        defaultClientShouldBeFound("phone.in=$DEFAULT_PHONE,$UPDATED_PHONE")

        // Get all the clientList where phone equals to UPDATED_PHONE
        defaultClientShouldNotBeFound("phone.in=$UPDATED_PHONE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByPhoneIsNullOrNotNull() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where phone is not null
        defaultClientShouldBeFound("phone.specified=true")

        // Get all the clientList where phone is null
        defaultClientShouldNotBeFound("phone.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByPhoneContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where phone contains DEFAULT_PHONE
        defaultClientShouldBeFound("phone.contains=" + DEFAULT_PHONE)

        // Get all the clientList where phone contains UPDATED_PHONE
        defaultClientShouldNotBeFound("phone.contains=" + UPDATED_PHONE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllClientsByPhoneNotContainsSomething() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        // Get all the clientList where phone does not contain DEFAULT_PHONE
        defaultClientShouldNotBeFound("phone.doesNotContain=" + DEFAULT_PHONE)

        // Get all the clientList where phone does not contain UPDATED_PHONE
        defaultClientShouldBeFound("phone.doesNotContain=" + UPDATED_PHONE)
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */

    @Throws(Exception::class)
    private fun defaultClientShouldBeFound(filter: String) {
        restClientMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(client.id?.toInt())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))

        // Check, that the count call also returns 1
        restClientMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"))
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    @Throws(Exception::class)
    private fun defaultClientShouldNotBeFound(filter: String) {
        restClientMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Check, that the count call also returns 0
        restClientMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingClient() {
        // Get the client
        restClientMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewClient() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        val databaseSizeBeforeUpdate = clientRepository.findAll().size

        // Update the client
        val updatedClient = clientRepository.findById(client.id).get()
        // Disconnect from session so that the updates on updatedClient are not directly saved in db
        em.detach(updatedClient)
        updatedClient.firstName = UPDATED_FIRST_NAME
        updatedClient.lastName = UPDATED_LAST_NAME
        updatedClient.email = UPDATED_EMAIL
        updatedClient.address = UPDATED_ADDRESS
        updatedClient.phone = UPDATED_PHONE

        restClientMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedClient.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedClient))
        ).andExpect(status().isOk)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
        val testClient = clientList[clientList.size - 1]
        assertThat(testClient.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testClient.lastName).isEqualTo(UPDATED_LAST_NAME)
        assertThat(testClient.email).isEqualTo(UPDATED_EMAIL)
        assertThat(testClient.address).isEqualTo(UPDATED_ADDRESS)
        assertThat(testClient.phone).isEqualTo(UPDATED_PHONE)
    }

    @Test
    @Transactional
    fun putNonExistingClient() {
        val databaseSizeBeforeUpdate = clientRepository.findAll().size
        client.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClientMockMvc.perform(
            put(ENTITY_API_URL_ID, client.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(client))
        )
            .andExpect(status().isBadRequest)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchClient() {
        val databaseSizeBeforeUpdate = clientRepository.findAll().size
        client.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(client))
        ).andExpect(status().isBadRequest)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamClient() {
        val databaseSizeBeforeUpdate = clientRepository.findAll().size
        client.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(client))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateClientWithPatch() {

        // Initialize the database
        clientRepository.saveAndFlush(client)

        val databaseSizeBeforeUpdate = clientRepository.findAll().size

// Update the client using partial update
        val partialUpdatedClient = Client().apply {
            id = client.id

            firstName = UPDATED_FIRST_NAME
            lastName = UPDATED_LAST_NAME
            phone = UPDATED_PHONE
        }

        restClientMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedClient.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedClient))
        )
            .andExpect(status().isOk)

// Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
        val testClient = clientList.last()
        assertThat(testClient.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testClient.lastName).isEqualTo(UPDATED_LAST_NAME)
        assertThat(testClient.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(testClient.address).isEqualTo(DEFAULT_ADDRESS)
        assertThat(testClient.phone).isEqualTo(UPDATED_PHONE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateClientWithPatch() {

        // Initialize the database
        clientRepository.saveAndFlush(client)

        val databaseSizeBeforeUpdate = clientRepository.findAll().size

// Update the client using partial update
        val partialUpdatedClient = Client().apply {
            id = client.id

            firstName = UPDATED_FIRST_NAME
            lastName = UPDATED_LAST_NAME
            email = UPDATED_EMAIL
            address = UPDATED_ADDRESS
            phone = UPDATED_PHONE
        }

        restClientMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedClient.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedClient))
        )
            .andExpect(status().isOk)

// Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
        val testClient = clientList.last()
        assertThat(testClient.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testClient.lastName).isEqualTo(UPDATED_LAST_NAME)
        assertThat(testClient.email).isEqualTo(UPDATED_EMAIL)
        assertThat(testClient.address).isEqualTo(UPDATED_ADDRESS)
        assertThat(testClient.phone).isEqualTo(UPDATED_PHONE)
    }

    @Throws(Exception::class)
    fun patchNonExistingClient() {
        val databaseSizeBeforeUpdate = clientRepository.findAll().size
        client.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClientMockMvc.perform(
            patch(ENTITY_API_URL_ID, client.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(client))
        )
            .andExpect(status().isBadRequest)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchClient() {
        val databaseSizeBeforeUpdate = clientRepository.findAll().size
        client.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(client))
        )
            .andExpect(status().isBadRequest)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamClient() {
        val databaseSizeBeforeUpdate = clientRepository.findAll().size
        client.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(client))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Client in the database
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteClient() {
        // Initialize the database
        clientRepository.saveAndFlush(client)

        val databaseSizeBeforeDelete = clientRepository.findAll().size

        // Delete the client
        restClientMockMvc.perform(
            delete(ENTITY_API_URL_ID, client.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val clientList = clientRepository.findAll()
        assertThat(clientList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_FIRST_NAME = "AAAAAAAAAA"
        private const val UPDATED_FIRST_NAME = "BBBBBBBBBB"

        private const val DEFAULT_LAST_NAME = "AAAAAAAAAA"
        private const val UPDATED_LAST_NAME = "BBBBBBBBBB"

        private const val DEFAULT_EMAIL = "AAAAAAAAAA"
        private const val UPDATED_EMAIL = "BBBBBBBBBB"

        private const val DEFAULT_ADDRESS = "AAAAAAAAAA"
        private const val UPDATED_ADDRESS = "BBBBBBBBBB"

        private const val DEFAULT_PHONE = "AAAAAAAAAA"
        private const val UPDATED_PHONE = "BBBBBBBBBB"

        private val ENTITY_API_URL: String = "/api/clients"
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
        fun createEntity(em: EntityManager): Client {
            val client = Client(

                firstName = DEFAULT_FIRST_NAME,

                lastName = DEFAULT_LAST_NAME,

                email = DEFAULT_EMAIL,

                address = DEFAULT_ADDRESS,

                phone = DEFAULT_PHONE

            )

            return client
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Client {
            val client = Client(

                firstName = UPDATED_FIRST_NAME,

                lastName = UPDATED_LAST_NAME,

                email = UPDATED_EMAIL,

                address = UPDATED_ADDRESS,

                phone = UPDATED_PHONE

            )

            return client
        }
    }
}
