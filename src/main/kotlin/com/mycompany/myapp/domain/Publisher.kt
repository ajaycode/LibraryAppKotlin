package com.mycompany.myapp.domain

import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Publisher.
 */
@Entity
@Table(name = "publisher")
data class Publisher(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @get: NotNull
    @get: Size(max = 100)
    @Column(name = "name", length = 100, nullable = false, unique = true)
    var name: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Publisher) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Publisher{" +
        "id=$id" +
        ", name='$name'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
