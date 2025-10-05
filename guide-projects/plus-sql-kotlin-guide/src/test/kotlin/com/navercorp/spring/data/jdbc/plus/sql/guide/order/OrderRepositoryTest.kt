/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2025 NAVER Corp.
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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * @author Myeonghyeon Lee
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderRepositoryTest {
    @Autowired
    lateinit var sut: OrderRepository

    val orders: List<Order> = listOf(
        Order(
            price = 1000,
            status = OrderStatus.PLACE,
            purchaserNo = "navercorp"
        ),
        Order(
            price = 5000,
            status = OrderStatus.PLACE,
            purchaserNo = "navercorp"
        ),
        Order(
            price = 3000,
            status = OrderStatus.COMPLETED,
            purchaserNo = "navercorp"
        )
    )

    @Test
    fun findByPurchaserNo() {
        // given
        sut.saveAll(orders)

        // when
        val actual = sut.findByPurchaserNo("navercorp")

        // then
        val sorted = actual.sortedBy { it.price }
        assertThat(sorted).hasSize(3)
        assertThat(sorted[0].price).isEqualTo(1000L)
        assertThat(sorted[0].status).isEqualTo(OrderStatus.PLACE)
        assertThat(sorted[1].price).isEqualTo(3000L)
        assertThat(sorted[1].status).isEqualTo(OrderStatus.COMPLETED)
        assertThat(sorted[2].price).isEqualTo(5000L)
        assertThat(sorted[2].status).isEqualTo(OrderStatus.PLACE)
    }

    @Test
    fun findByPurchaserNoAndStatusAndPrice() {
        // given
        sut.saveAll(orders)
        val criteria = OrderCriteria(
            purchaserNo = "navercorp",
            status = OrderStatus.PLACE,
            sortBy = OrderSort.PRICE
        )

        // when
        val actual = sut.findByPurchaserNoAndStatusAndPrice(criteria, 5000)

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0].price).isEqualTo(5000L)
        assertThat(actual[0].status).isEqualTo(OrderStatus.PLACE)
    }

    @Test
    fun search() {
        // given
        sut.saveAll(orders)
        val criteria = OrderCriteria(
            purchaserNo = "navercorp",
            status = OrderStatus.PLACE,
            sortBy = OrderSort.PRICE
        )

        // when
        val actual = sut.search(criteria)

        // then
        val sorted = actual.sortedBy { it.price }
        assertThat(sorted).hasSize(2)
        assertThat(sorted[0].price).isEqualTo(1000L)
        assertThat(sorted[0].status).isEqualTo(OrderStatus.PLACE)
        assertThat(sorted[1].price).isEqualTo(5000L)
        assertThat(sorted[1].status).isEqualTo(OrderStatus.PLACE)
    }

    @Test
    fun countByPurchaserNo() {
        // given
        sut.saveAll(orders)

        // when
        val actual: Long = sut.countByPurchaserNo("navercorp")

        // then
        assertThat(actual.toInt()).isEqualTo(orders.size)
    }
}
