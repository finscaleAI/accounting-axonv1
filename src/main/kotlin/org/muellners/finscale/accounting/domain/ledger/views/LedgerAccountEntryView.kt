package org.muellners.finscale.accounting.domain.ledger.views

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.muellners.finscale.accounting.domain.enumeration.TransactionSide
import org.muellners.finscale.accounting.domain.journal.views.TransactionView

/**
 * A LedgerAccountEntry.
 */
@Entity
@Table(name = "ledger_account_entry")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class LedgerAccountEntryView(
    @Id
    var id: UUID? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    var side: TransactionSide? = null,

    @get: NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    var amount: BigDecimal? = null,

    @get: NotNull
    @Column(name = "balance", precision = 21, scale = 2, nullable = false)
    var balance: BigDecimal? = null,

    @get: NotNull
    @Column(name = "processed_on", nullable = false)
    var processedOn: LocalDate? = null,

    @OneToOne @JoinColumn(unique = true)
    var transaction: TransactionView? = null,

    @ManyToOne @JsonIgnoreProperties(value = ["entries"], allowSetters = true)
    var ledgerAccount: LedgerAccountView? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerAccountEntryView) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "LedgerAccountEntry{" +
        "id=$id" +
        ", side='$side'" +
        ", amount=$amount" +
        ", balance=$balance" +
        ", processedOn='$processedOn'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
