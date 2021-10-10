package com.mycompany.myapp.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Book.
 */
@Entity
@Table(name = "book")
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @get: NotNull
    @get: Size(min = 5, max = 13)
    @Column(name = "isbn", length = 13, nullable = false, unique = true)
    var isbn: String? = null,

    @get: NotNull
    @get: Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    var name: String? = null,

    @get: NotNull
    @get: Size(min = 4, max = 50)
    @Column(name = "publish_year", length = 50, nullable = false)
    var publishYear: String? = null,

    @get: NotNull
    @Column(name = "copies", nullable = false)
    var copies: Int? = null,

    @Lob
    @Column(name = "cover")
    var cover: ByteArray? = null,

    @Column(name = "cover_content_type")
    var coverContentType: String? = null,

    @OneToOne @JoinColumn(unique = true)
    var publisher: Publisher? = null,

    @ManyToMany
    @JoinTable(
        name = "rel_book__author",
        joinColumns = [
            JoinColumn(name = "book_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "author_id")
        ]
    )
    @JsonIgnoreProperties(
        value = [
            "books"
        ],
        allowSetters = true
    )
    var authors: MutableSet<Author>? = mutableSetOf(),

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addAuthor(author: Author): Book {
        if (this.authors == null) {
            this.authors = mutableSetOf()
        }
        this.authors?.add(author)
        author.books?.add(this)
        return this
    }

    fun removeAuthor(author: Author): Book {
        this.authors?.remove(author)
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Book{" +
        "id=$id" +
        ", isbn='$isbn'" +
        ", name='$name'" +
        ", publishYear='$publishYear'" +
        ", copies=$copies" +
        ", cover='$cover'" +
        ", coverContentType='$coverContentType'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
