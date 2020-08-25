package org.muellners.finscale.accounting.domain.ledger.views

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.muellners.finscale.accounting.domain.enumeration.AccountState
import org.muellners.finscale.accounting.domain.enumeration.LedgerType

/**
 * A LedgerAccount.
 */
@Entity
@Table(name = "ledger_account")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class LedgerAccountView(
    @Id
    var id: UUID? = null,

    @get: NotNull
    @Column(name = "identifier", nullable = false, unique = true)
    var identifier: String? = null,

    @Column(name = "alternative_account_number", unique = true)
    var alternativeAccountNumber: String? = null,

    @get: NotNull
    @Column(name = "name", nullable = false)
    var name: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: LedgerType? = null,

    @get: NotNull
    @Column(name = "holders", nullable = false)
    var holders: String? = null,

    @get: NotNull
    @Column(name = "signature_authorities", nullable = false)
    var signatureAuthorities: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    var state: AccountState? = null,

    @Column(name = "balance", precision = 21, scale = 2)
    var balance: BigDecimal? = null,

    @OneToMany(mappedBy = "ledgerAccount")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    var actions: MutableSet<LedgerAccountActionView> = mutableSetOf(),

    @OneToMany(mappedBy = "ledgerAccount", cascade = [CascadeType.ALL])
//    @OneToMany(mappedBy = "ledgerAccount", cascade = [CascadeType.PERSIST])
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    var entries: MutableSet<LedgerAccountEntryView> = mutableSetOf(),

    @ManyToOne @JsonIgnoreProperties(value = ["ledgerAccounts"], allowSetters = true)
    var ledger: LedgerView? = null,

    @ManyToOne @JsonIgnoreProperties(value = ["ledgerAccounts"], allowSetters = true)
    var referenceAccount: LedgerAccountView? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addActions(ledgerAccountAction: LedgerAccountActionView): LedgerAccountView {
        this.actions.add(ledgerAccountAction)
        ledgerAccountAction.ledgerAccount = this
        return this
    }

    fun removeActions(ledgerAccountAction: LedgerAccountActionView): LedgerAccountView {
        this.actions.remove(ledgerAccountAction)
        ledgerAccountAction.ledgerAccount = null
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerAccountView) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "LedgerAccount{" +
        "id=$id" +
        ", identifier='$identifier'" +
        ", alternativeAccountNumber='$alternativeAccountNumber'" +
        ", name='$name'" +
        ", type='$type'" +
        ", holders='$holders'" +
        ", signatureAuthorities='$signatureAuthorities'" +
        ", state='$state'" +
        ", balance=$balance" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
