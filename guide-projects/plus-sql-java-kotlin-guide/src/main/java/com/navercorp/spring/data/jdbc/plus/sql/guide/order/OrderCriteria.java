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

import lombok.Builder;

/**
 *
 * public for OrderSql.kt access
 * Kotlin can not access Java Class's lombok generated methods.
 * https://d2.naver.com/helloworld/6685007
 *
 * 1. public field modifier (with final)
 * 2. Write getter method.
 * 3. Change kotlin(.kt) class
 *
 * @author Myeonghyeon Lee
 */
@Builder
public class OrderCriteria {
	public final String purchaserNo;
	public final OrderStatus status;
	public final OrderSort sortBy;

	public enum OrderSort {
		ID, PRICE
	}
}
