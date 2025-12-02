package com.navercorp.spring.data.jdbc.plus.repository.guide.coupon;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Table("coupon")
@Builder(toBuilder = true)
public record Coupon(
	@Id
	@Embedded.Nullable
	CouponId id,
	@Column("discount_amt")
	long discountAmount,
	String purchaserNo
) {
	public record CouponId(
		String couponId,
		String pubtId
	) {
		public static CouponId generate() {
			return new CouponId(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
			);
		}
	}
}
