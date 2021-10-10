package com.mycompany.myapp.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

/**
 * A BorrowedBook.
 */
@Entity
@Table(name = "borrowed_book")
data class BorrowedBook(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "borrow_date")
    var borrowDate: LocalDate? = null,

    @JsonIgnoreProperties(
        value = [
            "publisher", "authors"
        ],
        allowSetters = true
    )
    @OneToOne @JoinColumn(unique = true)
    var book: Book? = null,

    @OneToOne @JoinColumn(unique = true)
    var client: Client? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BorrowedBook) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "BorrowedBook{" +
        "id=$id" +
        ", borrowDate='$borrowDate'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
