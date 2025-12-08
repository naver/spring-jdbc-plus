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

package com.navercorp.spring.data.jdbc.plus.repository.guide.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
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
public class OrderRepositoryTest {
	@Autowired
	private OrderRepository sut;

	private List<Order> orders = Arrays.asList(
		Order.builder()
			.status(OrderStatus.PLACE)
			.purchaserId("navercorp")
			.discount(OrderDiscount.builder()
				.originPrice(BigDecimal.valueOf(10000))
				.discountPrice(BigDecimal.valueOf(1000))
				.build())
			.items(Arrays.asList(
				OrderItem.builder()
					.name("window")
					.productNo("1")
					.pricing(
						Pricing.builder()
							.price(BigDecimal.valueOf(3000))
							.quantity(2)
							.build())
					.sellerId("smart-store")
					.build(),
				OrderItem.builder()
					.name("checkout")
					.productNo("2")
					.pricing(
						Pricing.builder()
							.price(BigDecimal.valueOf(1000))
							.quantity(4)
							.build())
					.sellerId("naver-pay")
					.build()
			))
			.build(),
		Order.builder()
			.status(OrderStatus.PLACE)
			.purchaserId("navercorp")
			.discount(OrderDiscount.builder()
				.originPrice(BigDecimal.valueOf(5000))
				.discountPrice(BigDecimal.valueOf(100))
				.build())
			.items(Collections.singletonList(
				OrderItem.builder()
					.name("pencil")
					.productNo("3")
					.pricing(
						Pricing.builder()
							.price(BigDecimal.valueOf(1000))
							.quantity(5)
							.build())
					.sellerId("smart-store")
					.build()
			))
			.build(),
		Order.builder()
			.status(OrderStatus.COMPLETED)
			.purchaserId("navercorp")
			.discount(OrderDiscount.builder()
				.originPrice(BigDecimal.valueOf(12000))
				.discountPrice(BigDecimal.valueOf(3000))
				.build())
			.items(Arrays.asList(
				OrderItem.builder()
					.name("coke")
					.productNo("4")
					.pricing(
						Pricing.builder()
							.price(BigDecimal.valueOf(1000))
							.quantity(6)
							.build())
					.sellerId("smart-store")
					.build(),
				OrderItem.builder()
					.name("sprite")
					.productNo("5")
					.pricing(
						Pricing.builder()
							.price(BigDecimal.valueOf(1000))
							.quantity(6)
							.build())
					.sellerId("naver-pay")
					.build()
			))
			.build());

	@Test
	void insert() {
		// when
		Iterable<Order> actual = this.sut.insertAll(orders);

		// then
		actual.forEach(order -> {
			assertThat(order.getId()).isNotNull();
			assertThat(order.getDiscount().getId()).isNotNull();
			order.getItems().forEach(item -> assertThat(item.getId()).isNotNull());
		});
	}

	@Test
	void update() {
		// given
		Order order = this.sut.insert(orders.get(0));
		order.complete();

		// when
		Order actual = this.sut.update(order);

		// then
		assertThat(actual.getStatus()).isEqualTo(OrderStatus.COMPLETED);
	}

	@Test
	void findById() {
		// given
		Order order = this.sut.insert(orders.get(0));

		// when
		Optional<Order> actual = this.sut.findById(order.getId());

		// then
		assertThat(actual).isNotEmpty();
		assertThat(actual.get().getId()).isEqualTo(order.getId());
	}
}
