package com.navercorp.spring.data.jdbc.plus.sql.guide.coupon;

import org.springframework.data.repository.CrudRepository;

import com.navercorp.spring.data.jdbc.plus.sql.guide.coupon.CouponPubt.PubtPk;

public interface CouponPubtRepository extends CrudRepository<CouponPubt, PubtPk> {
}

