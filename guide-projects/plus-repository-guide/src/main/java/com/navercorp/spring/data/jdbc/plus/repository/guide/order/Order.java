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

package com.navercorp.spring.data.jdbc.plus.repository.guide.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

/**
 * @author Myeonghyeon Lee
 */
@Table("n_order")
@Getter
@Builder
@AllArgsConstructor
public final class Order {
	@Id
	@With
	private final Long id;
	private final String purchaserId;
	private OrderStatus status;
	@Column("order_id")
	private OrderDiscount discount;

	@MappedCollection(idColumn = "order_id", keyColumn = "idx")
	@Builder.Default
	private List<OrderItem> items = new ArrayList<>();

	public void complete() {
		this.status = OrderStatus.COMPLETED;
	}
}
