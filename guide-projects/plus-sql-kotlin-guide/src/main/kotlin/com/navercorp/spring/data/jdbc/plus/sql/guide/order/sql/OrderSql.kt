package com.navercorp.spring.data.jdbc.plus.sql.guide.order.sql

import com.navercorp.spring.data.jdbc.plus.sql.guide.order.Order
import com.navercorp.spring.data.jdbc.plus.sql.guide.order.OrderCriteria
import com.navercorp.spring.data.jdbc.plus.sql.guide.order.OrderSort
import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class OrderSql : SqlGeneratorSupport() {
    fun selectByPurchaserNo(): String = """
        SELECT ${sql.columns(Order::class.java)} 
        FROM ${sql.tables(Order::class.java)}
        WHERE purchaser_no = :purchaserNo
        """

    fun search(criteria: OrderCriteria): String =
        """
        SELECT ${sql.columns(Order::class.java)} 
        FROM ${sql.tables(Order::class.java)}
        WHERE purchaser_no = :purchaserNo
        AND status = :status
        ${when (criteria.sortBy) {
            OrderSort.ID -> "ORDER BY id"
            OrderSort.PRICE -> "ORDER BY price"
            else -> ""
        }}
        """

    fun countByPurchaserNo(): String =
        """
        SELECT count(*) 
        FROM ${sql.tables(Order::class.java)}
        WHERE purchaser_no = :purchaserNo
        """
}
