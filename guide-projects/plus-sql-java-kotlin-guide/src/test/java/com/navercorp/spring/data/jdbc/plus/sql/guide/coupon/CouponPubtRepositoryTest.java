package com.navercorp.spring.data.jdbc.plus.sql.guide.coupon;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.spring.data.jdbc.plus.sql.guide.coupon.CouponPubt.PubtPk;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CouponPubtRepositoryTest {
	@Autowired
	private CouponPubtRepository sut;

	@Test
	void saveAndFind() {
		// given
		PubtPk id = new PubtPk("pubt-1", 1L);
		Coupon coupon = new Coupon("cupn-1", BigDecimal.TEN, id);
		CouponPubt couponPubt = new CouponPubt(id, BigDecimal.valueOf(100), Set.of(coupon));

		// when
		sut.save(couponPubt);
		CouponPubt actual = sut.findById(id).orElseThrow();

		// then
		assertThat(actual.pubtPk()).isEqualTo(id);
		assertThat(actual.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
		assertThat(actual.coupons()).hasSize(1);

		Coupon actualCoupon = actual.coupons().iterator().next();
		assertThat(actualCoupon.couponNo()).isEqualTo("cupn-1");
		assertThat(actualCoupon.discountAmount()).isEqualByComparingTo(BigDecimal.TEN);
		assertThat(actualCoupon.pubtPk()).isEqualTo(id);
	}
}

