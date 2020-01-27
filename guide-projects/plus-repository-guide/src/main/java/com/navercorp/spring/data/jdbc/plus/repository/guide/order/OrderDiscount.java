package com.navercorp.spring.data.jdbc.plus.repository.guide.order;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Table("n_order_discount")
@Value
@Builder
public class OrderDiscount {
	@Id
	@With
	private Long id;

	private BigDecimal originPrice;

	private BigDecimal discountPrice;
}
