package org.muellners.finscale.accounting.domain.journal.views

import java.io.Serializable
import java.util.*
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A TransactionType.
 */
@Entity
@Table(name = "transaction_type")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class TransactionTypeView(
    @Id
    var id: UUID? = null,

    @get: NotNull
    @Column(name = "identifier", nullable = false, unique = true)
    var identifier: String? = null,

    @get: NotNull
    @Column(name = "name", nullable = false)
    var name: String? = null,

    @Column(name = "description")
    var description: String? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionTypeView) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "TransactionType{" +
        "id=$id" +
        ", identifier='$identifier'" +
        ", name='$name'" +
        ", description='$description'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
