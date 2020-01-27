package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderCriteria {
	private String purchaserNo;
	private OrderStatus status;
	private OrderSort sortBy;

	public enum OrderSort {
		ID, PRICE
	}
}
