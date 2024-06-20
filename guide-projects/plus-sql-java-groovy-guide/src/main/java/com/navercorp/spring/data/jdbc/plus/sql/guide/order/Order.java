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

import javax.annotation.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.ObjectUtils;

import lombok.Builder;
import lombok.Getter;

import com.navercorp.spring.data.jdbc.plus.sql.annotation.SqlFunction;
import com.navercorp.spring.data.jdbc.plus.sql.guide.support.EmptyStringToNullTraits;

/**
 * @author Myeonghyeon Lee
 */
@Table("n_order")
@Getter
@Builder(toBuilder = true)
public class Order implements EmptyStringToNullTraits {
	@Id
	private Long id;

	@SqlFunction(expressions = {SqlFunction.COLUMN_NAME, "0"})
	private Long price;

	private OrderStatus status;

	private String purchaserNo;

	private String name;

	@Nullable
	@Embedded.Nullable(prefix = "discount_")
	private Discount discount;

	public void complete() {
		this.status = OrderStatus.COMPLETED;
	}

	@Builder
	public record Discount(
		@Nullable
		Long amount,

		@Embedded.Nullable
		DiscountType type
	) {
	}

	@Builder
	public record DiscountType(
		String type
	) {
	}

	@Override
	public Order emptyStringToNull() {
		return this.toBuilder()
			.name(ObjectUtils.isEmpty(this.name) ? null : this.name)
			.build();
	}
}
