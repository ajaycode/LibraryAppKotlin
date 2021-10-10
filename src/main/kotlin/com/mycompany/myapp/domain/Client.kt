package com.mycompany.myapp.domain

import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Client.
 */
@Entity
@Table(name = "client")
data class Client(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @get: NotNull
    @get: Size(max = 50)
    @Column(name = "first_name", length = 50, nullable = false)
    var firstName: String? = null,

    @get: NotNull
    @get: Size(max = 50)
    @Column(name = "last_name", length = 50, nullable = false)
    var lastName: String? = null,

    @get: Size(max = 50)
    @Column(name = "email", length = 50, unique = true)
    var email: String? = null,

    @get: Size(max = 50)
    @Column(name = "address", length = 50)
    var address: String? = null,

    @get: Size(max = 20)
    @Column(name = "phone", length = 20)
    var phone: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Client) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Client{" +
        "id=$id" +
        ", firstName='$firstName'" +
        ", lastName='$lastName'" +
        ", email='$email'" +
        ", address='$address'" +
        ", phone='$phone'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
