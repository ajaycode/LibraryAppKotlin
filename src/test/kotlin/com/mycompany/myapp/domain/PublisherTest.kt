package com.mycompany.myapp.domain

import com.mycompany.myapp.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PublisherTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Publisher::class)
        val publisher1 = Publisher()
        publisher1.id = 1L
        val publisher2 = Publisher()
        publisher2.id = publisher1.id
        assertThat(publisher1).isEqualTo(publisher2)
        publisher2.id = 2L
        assertThat(publisher1).isNotEqualTo(publisher2)
        publisher1.id = null
        assertThat(publisher1).isNotEqualTo(publisher2)
    }
}
