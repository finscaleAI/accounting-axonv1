package org.muellners.finscale.accounting.domain.journal.views

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.ledger.views.LedgerAccountView

/**
 * A Transaction.
 */
@Entity
@Table(name = "transaction")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class TransactionView(
    @Id
    var id: UUID? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_side", nullable = false)
    var transactionSide: TransactionSide? = null,

    @get: NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    var amount: BigDecimal? = null,

    @ManyToOne @JsonIgnoreProperties(value = ["transactions"], allowSetters = true)
    var account: LedgerAccountView? = null,

    @ManyToOne @JsonIgnoreProperties(value = ["transactions"], allowSetters = true)
    var journalEntry: JournalEntryView? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionView) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Transaction{" +
        "id=$id" +
        ", transactionSide='$transactionSide'" +
        ", amount=$amount" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
