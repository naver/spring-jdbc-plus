package com.navercorp.spring.data.jdbc.plus.sql.guide.order

import com.navercorp.spring.data.jdbc.plus.sql.guide.order.sql.OrderSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait
import org.springframework.data.repository.CrudRepository

interface OrderRepository : CrudRepository<Order, Long>, OrderRepositoryCustom

interface OrderRepositoryCustom {
    fun findByPurchaserNo(purchaserNo: String): List<Order>

    fun search(criteria: OrderCriteria): List<Order>

    fun countByPurchaserNo(purchaserNo: String): Long
}

class OrderRepositoryImpl(entityProvider: EntityJdbcProvider) : JdbcRepositorySupport<Order>(
    Order::class.java,
    entityProvider
), OrderRepositoryCustom, SingleValueSelectTrait {
    private val sqls: OrderSql = super.sqls(::OrderSql)

    override fun findByPurchaserNo(purchaserNo: String): List<Order> {
        val sql = this.sqls.selectByPurchaserNo()
        return find(
            sql, mapParameterSource()
                .addValue("purchaserNo", purchaserNo)
        )
    }

    override fun search(criteria: OrderCriteria): List<Order> {
        val sql = this.sqls.search(criteria)
        return find(
            sql, mapParameterSource()
                .addValue("purchaserNo", criteria.purchaserNo)
                .addValue("status", criteria.status.name)
        )
    }

    override fun countByPurchaserNo(purchaserNo: String): Long {
        val sql = this.sqls.countByPurchaserNo()
        return selectSingleValue(
            sql, mapParameterSource()
                .addValue("purchaserNo", purchaserNo),
            Long::class.java
        )
    }
}
