package com.navercorp.spring.data.jdbc.plus.sql.guide.test;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;

import org.jspecify.annotations.NonNull;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.spring.data.jdbc.plus.sql.guide.order.Order;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OrderSpec {
	static final ArbitraryBuilderCandidate<Order> ORDER_INSTANCE = createOrder();

	private static final Arbitrary<@NonNull String> PURCHASER_NO_ARBITRARY =
		Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(10);

	private static ArbitraryBuilderCandidate<Order> createOrder() {
		return ArbitraryBuilderCandidateFactory.of(Order.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(Order::id))
					.set(javaGetter(Order::purchaserNo), PURCHASER_NO_ARBITRARY)
			);
	}
}
