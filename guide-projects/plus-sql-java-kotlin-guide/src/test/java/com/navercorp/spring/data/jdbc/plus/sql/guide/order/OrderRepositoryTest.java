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

package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Myeonghyeon Lee
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryTest {
	@Autowired
	private OrderRepository sut;

	private List<Order> orders = Arrays.asList(
		Order.builder()
			.price(1000L)
			.status(OrderStatus.PLACE)
			.purchaserNo("navercorp")
			.build(),
		Order.builder()
			.price(5000L)
			.status(OrderStatus.PLACE)
			.purchaserNo("navercorp")
			.build(),
		Order.builder()
			.price(3000L)
			.status(OrderStatus.COMPLETED)
			.purchaserNo("navercorp")
			.build());

	@Test
	void findById() {
		// given
		sut.saveAll(orders);

		// when
		Optional<Order> actual = sut.findById(orders.get(0).getId());

		// then
		Assertions.assertThat(actual).isPresent();
		assertThat(actual.get().getId()).isEqualTo(orders.get(0).getId());
	}

	@Test
	void findByPurchaserNo() {
		// given
		sut.saveAll(orders);

		// when
		List<Order> actual = sut.findByPurchaserNo("navercorp");

		// then
		actual.sort(comparing(Order::getPrice));
		Assertions.assertThat(actual).hasSize(3);
		assertThat(actual.get(0).getPrice()).isEqualTo(1000L);
		assertThat(actual.get(0).getStatus()).isEqualTo(OrderStatus.PLACE);
		assertThat(actual.get(1).getPrice()).isEqualTo(3000L);
		assertThat(actual.get(1).getStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(actual.get(2).getPrice()).isEqualTo(5000L);
		assertThat(actual.get(2).getStatus()).isEqualTo(OrderStatus.PLACE);
	}

	@Test
	void search() {
		// given
		sut.saveAll(orders);
		OrderCriteria criteria = OrderCriteria.builder()
			.purchaserNo("navercorp")
			.status(OrderStatus.PLACE)
			.sortBy(OrderCriteria.OrderSort.PRICE)
			.build();

		// when
		List<Order> actual = sut.search(criteria);

		// then
		actual.sort(comparing(Order::getPrice));
		Assertions.assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getPrice()).isEqualTo(1000L);
		assertThat(actual.get(0).getStatus()).isEqualTo(OrderStatus.PLACE);
		assertThat(actual.get(1).getPrice()).isEqualTo(5000L);
		assertThat(actual.get(1).getStatus()).isEqualTo(OrderStatus.PLACE);
	}

	@Test
	void countByPurchaserNo() {
		// given
		sut.saveAll(orders);

		// when
		long actual = sut.countByPurchaserNo("navercorp");

		// then
		assertThat(actual).isEqualTo(orders.size());
	}
}
