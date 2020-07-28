package org.muellners.finscale.accounting.domain.ledger.views

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A LedgerView.
 */
@Entity
@Table(name = "ledger_view")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class LedgerView(
    @Id
    var id: String? = null,
    @get: NotNull
    @Column(name = "identifier", nullable = false, unique = true)
    var identifier: String? = null,

    @get: NotNull
    @Column(name = "name", nullable = false)
    var name: String? = null,

    @Column(name = "type")
    var type: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "total_value", precision = 21, scale = 2)
    var totalValue: BigDecimal? = null,

    @Column(name = "show_accounts_in_chart")
    var showAccountsInChart: Boolean? = null,

    @ManyToOne @JsonIgnoreProperties(value = ["ledgerViews"], allowSetters = true)
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

    override fun toString() = "LedgerView{" +
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
