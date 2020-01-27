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
