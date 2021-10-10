package com.mycompany.myapp.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Author.
 */
@Entity
@Table(name = "author")
data class Author(
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

    @ManyToMany(mappedBy = "authors")

    @JsonIgnoreProperties(
        value = [
            "publisher", "authors"
        ],
        allowSetters = true
    )
    var books: MutableSet<Book>? = mutableSetOf(),

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addBook(book: Book): Author {
        if (this.books == null) {
            this.books = mutableSetOf()
        }
        this.books?.add(book)
        book.authors?.add(this)
        return this
    }

    fun removeBook(book: Book): Author {
        this.books?.remove(book)
        book.authors?.remove(this)
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Author) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Author{" +
        "id=$id" +
        ", firstName='$firstName'" +
        ", lastName='$lastName'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
