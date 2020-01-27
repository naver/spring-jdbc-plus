package com.navercorp.spring.data.jdbc.plus.sql.guide.order

data class OrderCriteria(
    val purchaserNo: String,
    val status: OrderStatus,
    val sortBy: OrderSort? = null
)

enum class OrderSort {
    ID, PRICE
}
