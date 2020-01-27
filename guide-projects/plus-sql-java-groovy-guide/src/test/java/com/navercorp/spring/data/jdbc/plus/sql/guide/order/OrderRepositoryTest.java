package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
