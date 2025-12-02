package com.navercorp.spring.data.jdbc.plus.repository.guide.coupon;

import java.util.List;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;
import com.navercorp.spring.data.jdbc.plus.repository.guide.coupon.Coupon.CouponId;

public interface CouponRepository extends JdbcRepository<Coupon, CouponId> {
	List<Coupon> findByPurchaserNo(String purchaserNo);
}
