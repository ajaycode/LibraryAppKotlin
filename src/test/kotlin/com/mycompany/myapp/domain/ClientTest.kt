package com.mycompany.myapp.domain

import com.mycompany.myapp.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Client::class)
        val client1 = Client()
        client1.id = 1L
        val client2 = Client()
        client2.id = client1.id
        assertThat(client1).isEqualTo(client2)
        client2.id = 2L
        assertThat(client1).isNotEqualTo(client2)
        client1.id = null
        assertThat(client1).isNotEqualTo(client2)
    }
}
