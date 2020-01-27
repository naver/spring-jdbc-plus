package com.navercorp.spring.data.jdbc.plus.repository.guide.order;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public final class Pricing {
	private final long quantity;

	@Builder.Default
	private final BigDecimal price = BigDecimal.ZERO;

	public Pricing add(Pricing pricing) {
		return Pricing.builder()
			.quantity(this.quantity + pricing.getQuantity())
			.price(this.price.add(pricing.getPrice()))
			.build();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Pricing pricing = (Pricing)obj;
		return Objects.equals(quantity, pricing.quantity)
			&& Objects.equals(price.stripTrailingZeros(), pricing.price.stripTrailingZeros());
	}

	@Override
	public int hashCode() {
		return Objects.hash(quantity, price.stripTrailingZeros());
	}
}
