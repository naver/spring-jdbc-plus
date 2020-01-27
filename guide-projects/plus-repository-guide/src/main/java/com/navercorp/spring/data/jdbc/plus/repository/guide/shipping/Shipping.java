package com.navercorp.spring.data.jdbc.plus.repository.guide.shipping;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import com.navercorp.spring.data.jdbc.plus.repository.guide.order.Order;

@Table("n_shipping")
@Getter
@Builder
@AllArgsConstructor
public class Shipping {
	@Id
	@With
	private final UUID id;

	private final AggregateReference<Order, Long> orderId;

	private String receiverAddress;

	private String memo;

	public void changeMemo(String memo) {
		this.memo = memo;
	}
}
