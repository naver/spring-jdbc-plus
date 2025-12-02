package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class PayHistoryRepositoryTest {
	private static final AtomicLong ID_GENERATOR = new AtomicLong();

	@Autowired
	PayHistoryRepository sut;

	@Test
	void insert() {
		// given
		PayHistory payHistory = PayHistory.from(samplePay());

		// when
		var result = sut.save(payHistory);

		// then
		assertThat(sut.findById(result.id())).isPresent().hasValueSatisfying(actual -> {
			assertThat(actual.amount()).isEqualTo(payHistory.amount());
			assertThat(actual.isNew()).isFalse();
		});
	}

	@Test
	void findByOrderNo() {
		// given
		List<PayHistory> payHistories = List.of(
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay())
		);

		// when
		sut.saveAll(payHistories);

		// then
		assertThat(sut.findByOrderNo(940329L)).hasSize(5).allSatisfy(actual -> {
			assertThat(actual.amount()).isEqualTo(1000L);
			assertThat(actual.isNew()).isFalse();
		});
	}

	@Test
	void findByOrderId() {
		// given
		List<PayHistory> payHistories = List.of(
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay()),
			PayHistory.from(samplePay())
		);

		// when
		sut.saveAll(payHistories);

		// then
		assertThat(sut.findByOrderId(940329L)).hasSize(5).allSatisfy(actual -> {
			assertThat(actual.amount()).isEqualTo(1000L);
			assertThat(actual.isNew()).isFalse();
		});
	}

	Pay samplePay() {
		return Pay.builder()
			.id(ID_GENERATOR.incrementAndGet())
			.amount(BigDecimal.valueOf(1000L))
			.orderId(940329L)
			.payAdmissions(Set.of())
			.build();
	}
}

