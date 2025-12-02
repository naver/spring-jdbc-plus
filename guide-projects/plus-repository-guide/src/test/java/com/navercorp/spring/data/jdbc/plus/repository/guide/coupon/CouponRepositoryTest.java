package com.navercorp.spring.data.jdbc.plus.repository.guide.coupon;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.spring.data.jdbc.plus.repository.guide.coupon.Coupon.CouponId;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CouponRepositoryTest {

	@Autowired
	private CouponRepository sut;

	@Test
	void insert() {
		// given
		Coupon coupon = sample();

		// when
		sut.insert(coupon);

		// then
		assertThat(sut.findById(coupon.id())).hasValue(coupon);
	}

	@Test
	void update() {
		// given
		Coupon coupon = sample();

		// when
		sut.insert(coupon);
		sut.update(coupon.toBuilder()
			.discountAmount(40302L)
			.build());

		// then
		assertThat(sut.findById(coupon.id())).hasValueSatisfying(actual -> {
			assertThat(actual.id()).isEqualTo(coupon.id());
			assertThat(actual.discountAmount()).isEqualTo(40302L);
		});
	}

	@Test
	void findByPurchaserNo() {
		// given
		List<Coupon> coupons = List.of(
			sample(),
			sample(),
			sample(),
			sample(),
			sample()
		);

		sut.insertAll(coupons);

		// when
		List<Coupon> actual = sut.findByPurchaserNo("940329");

		// then
		assertThat(actual).hasSize(5).allSatisfy(it -> {
			assertThat(it).isIn(coupons);
		});
	}

	Coupon sample() {
		return new Coupon(CouponId.generate(), 1000L, "940329");
	}
}

