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

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Builder;

/**
 * @author Myeonghyeon Lee
 */
@Builder
public record Pricing(
	long quantity,
	BigDecimal price
) {
	public Pricing add(Pricing pricing) {
		return Pricing.builder()
			.quantity(this.quantity + pricing.quantity())
			.price(this.price.add(pricing.price()))
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
