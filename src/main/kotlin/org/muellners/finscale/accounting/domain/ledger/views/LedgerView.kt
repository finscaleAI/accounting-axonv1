package org.muellners.finscale.accounting.domain.ledger.views

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.muellners.finscale.accounting.domain.enumeration.LedgerType

/**
 * A Ledger.
 */
@Entity
@Table(name = "ledger")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class LedgerView(
    @Id
    var id: UUID? = null,

    @get: NotNull
    @Column(name = "identifier", nullable = false, unique = true)
    var identifier: String? = null,

    @get: NotNull
    @Column(name = "name", nullable = false)
    var name: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: LedgerType? = null,

    @Column(name = "description")
    var description: String? = null,

    @get: NotNull
    @Column(name = "total_value", precision = 21, scale = 2, nullable = false)
    var totalValue: BigDecimal = BigDecimal.ZERO,

    @get: NotNull
    @Column(name = "show_accounts_in_chart", nullable = false)
    var showAccountsInChart: Boolean? = null,

    @OneToMany(mappedBy = "parentLedger")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    var subledgers: MutableSet<LedgerView> = mutableSetOf(),

    @ManyToOne @JsonIgnoreProperties(value = ["ledgers"], allowSetters = true)
    var parentLedger: LedgerView? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerView) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Ledger{" +
        "id=$id" +
        ", identifier='$identifier'" +
        ", name='$name'" +
        ", type='$type'" +
        ", description='$description'" +
        ", totalValue=$totalValue" +
        ", showAccountsInChart='$showAccountsInChart'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
