package com.navercorp.spring.data.jdbc.plus.sql.guide.pay.sql

import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.PayHistory
import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class PayHistorySql : SqlGeneratorSupport() {
    fun selectByOrderNo() = """
        SELECT ${sql.columns(PayHistory::class.java)}
        FROM ${sql.tables(PayHistory::class.java)}
        WHERE order_id = :orderId
    """.trimIndent()
}
