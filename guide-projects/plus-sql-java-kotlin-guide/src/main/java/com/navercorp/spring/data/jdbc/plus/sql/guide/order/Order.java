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

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Getter;

import com.navercorp.spring.jdbc.plus.commons.annotations.SqlFunction;

/**
 * @author Myeonghyeon Lee
 */
@Table("n_order")
@Getter
@Builder
public class Order {
	@Id
	private Long id;

	@SqlFunction(expressions = {SqlFunction.COLUMN_NAME, "0"})
	private Long price;

	private OrderStatus status;

	private String purchaserNo;

	public void complete() {
		this.status = OrderStatus.COMPLETED;
	}
}
