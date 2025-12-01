package com.navercorp.spring.data.jdbc.plus.sql.guide.coupon;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.navercorp.spring.data.jdbc.plus.sql.guide.coupon.CouponPubt.PubtPk;

@Table("cupn")
public record Coupon(
	@Id
	@Column("cupn_no")
	String couponNo,
	@Column("discount_amt")
	BigDecimal discountAmount,
	PubtPk pubtPk
) {
}
