package com.navercorp.spring.data.jdbc.plus.repository.guide.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Table("n_order_item")
@Value
@Builder
@AllArgsConstructor
public class OrderItem {
	@Id
	@With
	private Long id;

	private String productNo;

	private String name;

	@Embedded.Nullable
	private Pricing pricing;

	private final String sellerId;
}
