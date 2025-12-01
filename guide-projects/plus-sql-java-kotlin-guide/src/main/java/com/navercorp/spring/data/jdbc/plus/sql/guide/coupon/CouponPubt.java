package com.navercorp.spring.data.jdbc.plus.sql.guide.coupon;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("cupn_pubt")
public record CouponPubt(
	@Id
	@Embedded.Nullable
	PubtPk pubtPk,
	@Column("discount_amt")
	BigDecimal discountAmount,
	@MappedCollection
	Set<Coupon> coupons
) {

	public record PubtPk(
		@Column("pubt_nm")
		String pubtName,
		@Column("ver")
		long version
	) {
	}
}
