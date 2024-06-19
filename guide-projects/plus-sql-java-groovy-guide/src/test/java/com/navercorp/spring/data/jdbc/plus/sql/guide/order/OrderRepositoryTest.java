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
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
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
			.name("")
			.build(),
		Order.builder()
			.price(5000L)
			.status(OrderStatus.PLACE)
			.purchaserNo("navercorp")
			.name("yd2")
			.discount(Order.Discount.builder()
				.type(new Order.DiscountType("coupon"))
				.build())
			.build(),
		Order.builder()
			.price(3000L)
			.status(OrderStatus.COMPLETED)
			.purchaserNo("navercorp")
			.name("chanwool")
			.discount(Order.Discount.builder()
				.amount(1000L)
				.type(new Order.DiscountType("coupon"))
				.build()
			).build(),
		Order.builder()
			.price(9000L)
			.status(OrderStatus.COMPLETED)
			.purchaserNo("navercorp")
			.name("chanhyeong")
			.discount(Order.Discount.builder()
				.amount(1000L)
				.build()
			).build()
	);

	@Test
	void findById() {
		// given
		var insertedOrder = sut.save(orders.get(0));

		// when
		Optional<Order> actual = sut.findById(insertedOrder.getId());

		// then
		assertThat(actual).hasValueSatisfying(it -> {
			assertThat(it.getId()).isEqualTo(insertedOrder.getId());
			assertThat(it.getDiscount()).isNull();
		});
	}

	@Test
	void findByPurchaserNo() {
		// given
		sut.saveAll(orders);

		// when
		List<Order> actual = sut.findByPurchaserNo("navercorp");

		// then
		actual.sort(comparing(Order::getPrice));
		assertThat(actual).hasSize(4);
		assertThat(actual.get(0).getPrice()).isEqualTo(1000L);
		assertThat(actual.get(0).getName()).isNull();
		assertThat(actual.get(0).getStatus()).isEqualTo(OrderStatus.PLACE);
		assertThat(actual.get(1).getPrice()).isEqualTo(3000L);
		assertThat(actual.get(1).getStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(actual.get(2).getPrice()).isEqualTo(5000L);
		assertThat(actual.get(2).getStatus()).isEqualTo(OrderStatus.PLACE);
	}

	@Test
	void updateOrderNameWithBeforeSaveConverter() {
		// given
		List<Order> samples = StreamUtils.createStreamFromIterator(sut.saveAll(orders).iterator()).toList();

		// when
		sut.updateName(new UpdatingOrderNameDto(samples.get(2).getId(), ""));

		// then
		assertThat(samples.get(2).getName()).isNotBlank();
		assertThat(sut.findById(samples.get(2).getId())).hasValueSatisfying(actual -> {
			assertThat(actual.getName()).isNull();
		});
	}

	@Test
	void findByIds() {
		// given
		var ids = Streamable.of(sut.saveAll(orders)).stream()
			.map(Order::getId)
			.toList();

		Map<Long, Order> orderByPrice = orders.stream()
			.collect(toMap(Order::getPrice, identity()));

		// when
		List<Order> actual = Streamable.of(sut.findAllById(ids)).toList();

		// then
		assertThat(actual).allSatisfy(it ->
			assertThat(it).usingRecursiveComparison()
				.ignoringFields("id", "name")
				.isEqualTo(orderByPrice.get(it.getPrice()))
		);
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
	void findByPurchaesrNoAndStatusAndPrice() {
		// given
		sut.saveAll(orders);
		OrderCriteria criteria = OrderCriteria.builder()
			.purchaserNo("navercorp")
			.status(OrderStatus.PLACE)
			.sortBy(OrderCriteria.OrderSort.PRICE)
			.build();

		// when
		List<Order> actual = sut.findByPurchaserNoAndStatusAndPrice(criteria, 1000L);

		// then
		Assertions.assertThat(actual).hasSize(1);
		assertThat(actual.get(0).getPrice()).isEqualTo(1000L);
		assertThat(actual.get(0).getStatus()).isEqualTo(OrderStatus.PLACE);
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
