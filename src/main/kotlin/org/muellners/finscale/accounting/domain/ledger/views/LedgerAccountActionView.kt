package org.muellners.finscale.accounting.domain.ledger.views

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.muellners.finscale.accounting.domain.enumeration.AccountAction

/**
 * A LedgerAccountAction.
 */
@Entity
@Table(name = "ledger_account_action")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class LedgerAccountActionView(
    @Id
    var id: UUID? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    var action: AccountAction? = null,

    @get: NotNull
    @Column(name = "comment", nullable = false)
    var comment: String? = null,

    @get: NotNull
    @Column(name = "occured_at", nullable = false)
    var occuredAt: ZonedDateTime? = null,

    @ManyToOne @JsonIgnoreProperties(value = ["actions"], allowSetters = true)
    var ledgerAccount: LedgerAccountView? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerAccountActionView) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "LedgerAccountAction{" +
        "id=$id" +
        ", action='$action'" +
        ", comment='$comment'" +
        ", occuredAt='$occuredAt'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
