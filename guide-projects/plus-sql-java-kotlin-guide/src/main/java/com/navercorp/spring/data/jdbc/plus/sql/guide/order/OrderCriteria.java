package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import lombok.Builder;

/**
 * public for OrderSql.kt access
 * Kotlin can not access Java Class's lombok generated methods.
 * https://d2.naver.com/helloworld/6685007
 *
 * 1. public field modifier (with final)
 * 2. Write getter method.
 * 3. Change kotlin(.kt) class
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
