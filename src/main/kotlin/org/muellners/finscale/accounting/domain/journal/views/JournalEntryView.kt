package org.muellners.finscale.accounting.domain.journal.views

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.muellners.finscale.accounting.domain.enumeration.TransactionState

/**
 * A JournalEntry.
 */
@Entity
@Table(name = "journal_entry")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class JournalEntryView(
    @Id
    var id: UUID? = null,

    @get: NotNull
    @Column(name = "transaction_date", nullable = false)
    var transactionDate: LocalDate? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_state", nullable = false)
    var transactionState: TransactionState? = null,

    @Column(name = "note")
    var note: String? = null,

    @Column(name = "message")
    var message: String? = null,

    @OneToMany(mappedBy = "journalEntry", cascade = [ CascadeType.ALL ])
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    var transactions: MutableSet<TransactionView> = mutableSetOf(),

    @ManyToOne @JsonIgnoreProperties(value = ["journalEntries"], allowSetters = true)
    var transactionType: TransactionTypeView? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JournalEntryView) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "JournalEntry{" +
        "id=$id" +
        ", transactionDate='$transactionDate'" +
        ", transactionState='$transactionState'" +
        ", note='$note'" +
        ", message='$message'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
