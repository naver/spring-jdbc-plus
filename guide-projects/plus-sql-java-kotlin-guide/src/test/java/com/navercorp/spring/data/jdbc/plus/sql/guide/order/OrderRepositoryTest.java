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

package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.spring.data.jdbc.plus.sql.guide.test.ArbitrarySpec.fixtureMonkey;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Optional;

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

	List<Order> samples = fixtureMonkey.giveMeBuilder(Order.class)
		.set(javaGetter(Order::purchaserNo), "navercorp")
		.sampleList(3);

	@Test
	void findById() {
		// given
		List<Order> orders = sut.saveAll(samples);

		// when
		Optional<Order> actual = sut.findById(orders.get(0).id());

		// then
		then(actual).isPresent();
		then(actual.get().id()).isEqualTo(orders.get(0).id());
	}

	@Test
	void findByPurchaserNo() {
		// given
		sut.saveAll(samples);

		// when
		List<Order> actual = sut.findByPurchaserNo("navercorp");

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(o -> o.purchaserNo().equals("navercorp"));
	}

	@Test
	void search() {
		// given
		sut.saveAll(samples);

		OrderCriteria criteria = OrderCriteria.builder()
			.purchaserNo("navercorp")
			.status(OrderStatus.PLACE)
			.sortBy(OrderCriteria.OrderSort.PRICE)
			.build();

		// when
		List<Order> actual = sut.search(criteria);

		// then
		then(actual).hasSize(
			(int)samples.stream()
				.filter(it -> it.status() == OrderStatus.PLACE)
				.count()
		);
		then(actual).allMatch(o -> o.purchaserNo().equals("navercorp"));
		then(actual).allMatch(o -> o.status().equals(OrderStatus.PLACE));
	}

	@Test
	void countByPurchaserNo() {
		// given
		sut.saveAll(samples);

		// when
		long actual = sut.countByPurchaserNo("navercorp");

		// then
		then(actual).isEqualTo(3);
	}
}
