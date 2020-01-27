package com.navercorp.spring.data.jdbc.plus.sql.guide.order

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("n_order")
data class Order(
    @Id val id: Long? = null,
    val price: Long? = 0,
    var status: OrderStatus,
    val purchaserNo: String
)

enum class OrderStatus {
    PLACE, COMPLETED
}
