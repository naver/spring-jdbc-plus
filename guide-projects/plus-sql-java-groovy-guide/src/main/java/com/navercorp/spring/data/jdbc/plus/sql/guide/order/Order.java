package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Getter;

import com.navercorp.spring.data.jdbc.plus.sql.annotation.SqlFunction;

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
