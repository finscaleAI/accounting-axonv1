package org.muellners.finscale.accounting.domain.enumeration

/**
 * The TransactionSide enumeration.
 */
enum class TransactionSide {
    DEBIT {
        override fun inverse(): TransactionSide = CREDIT
    },

    CREDIT {
        override fun inverse(): TransactionSide = DEBIT
    };

    abstract fun inverse(): TransactionSide
}
