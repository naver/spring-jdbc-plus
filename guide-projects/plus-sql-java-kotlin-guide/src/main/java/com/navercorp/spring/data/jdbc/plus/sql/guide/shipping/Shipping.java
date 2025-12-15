package com.navercorp.spring.data.jdbc.plus.sql.guide.shipping;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

import com.navercorp.spring.data.jdbc.plus.sql.guide.order.Order;

@Builder
@Table("n_shipping")
public record Shipping(
	@Id
	@Column("shipping_no")
	String shippingNo,

	@Column("order_no")
	AggregateReference<Order, Long> orderNo,

	@Column("sender_address")
	String senderAddress,

	@Column("receiver_address")
	String receiverAddress,

	String memo,

	Instant deliveredAt
) {
}
