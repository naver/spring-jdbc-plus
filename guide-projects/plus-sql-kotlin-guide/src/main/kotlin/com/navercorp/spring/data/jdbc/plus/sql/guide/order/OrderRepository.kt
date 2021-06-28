/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.jdbc.plus.sql.guide.order

import com.navercorp.spring.data.jdbc.plus.sql.guide.order.sql.OrderSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait
import org.springframework.data.repository.CrudRepository

/**
 * @author Myeonghyeon Lee
 */
interface OrderRepository : CrudRepository<Order, Long>, OrderRepositoryCustom

interface OrderRepositoryCustom {
    fun findByPurchaserNo(purchaserNo: String): List<Order>

    fun search(criteria: OrderCriteria): List<Order>

    fun countByPurchaserNo(purchaserNo: String): Long

    fun findByPurchaserNoAndStatusAndPrice(criteria: OrderCriteria, price: Long): List<Order>
}

class OrderRepositoryImpl(entityProvider: EntityJdbcProvider) : JdbcRepositorySupport<Order>(
    Order::class.java,
    entityProvider
), OrderRepositoryCustom, SingleValueSelectTrait {
    private val sqls: OrderSql = super.sqls(::OrderSql)

    override fun findByPurchaserNo(purchaserNo: String): List<Order> {
        val sql = this.sqls.selectByPurchaserNo()
        return find(
            sql,
            mapParameterSource()
                .addValue("purchaserNo", purchaserNo)
        )
    }

    override fun search(criteria: OrderCriteria): List<Order> {
        val sql = this.sqls.search(criteria)
        return find(
            sql,
            mapParameterSource()
                .addValue("purchaserNo", criteria.purchaserNo)
                .addValue("status", criteria.status.name)
        )
    }

    override fun countByPurchaserNo(purchaserNo: String): Long {
        val sql = this.sqls.countByPurchaserNo()
        return selectSingleValue(
            sql,
            mapParameterSource()
                .addValue("purchaserNo", purchaserNo),
            Long::class.java
        )
    }

    override fun findByPurchaserNoAndStatusAndPrice(criteria: OrderCriteria, price: Long): List<Order> {
        val sql = this.sqls.selectByPurchaserNoAndStatusAndPrice()
        return find(
            sql,
            compositeSqlParameterSource(
                beanParameterSource("criteria.", criteria),
                mapParameterSource()
                    .addValue("price", price)
            )
        )
    }
}
