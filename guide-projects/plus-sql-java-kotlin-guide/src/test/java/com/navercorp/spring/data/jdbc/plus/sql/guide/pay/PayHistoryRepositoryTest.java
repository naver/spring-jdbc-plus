package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.spring.data.jdbc.plus.sql.guide.test.ArbitrarySpec.fixtureMonkey;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class PayHistoryRepositoryTest {
	@Autowired
	PayHistoryRepository sut;

	ArbitraryBuilder<PayHistory> fixtures = fixtureMonkey.giveMeBuilder(PayHistory.class)
		.set(javaGetter(PayHistory::orderId), 940329L);

	@Test
	void insert() {
		// given
		PayHistory payHistory = fixtures.sample();

		// when
		var result = sut.save(payHistory);

		then(sut.findById(result.id())).isPresent().hasValueSatisfying(actual -> {
			then(actual.amount()).isEqualTo(payHistory.amount());
			then(actual.isNew()).isFalse();
		});
	}

	@Test
	void findByOrderNo() {
		// given
		List<PayHistory> payHistories = fixtures
			.set(javaGetter(PayHistory::amount), 1000L)
			.sampleList(5);

		// when
		sut.saveAll(payHistories);

		then(sut.findByOrderNo(940329L)).hasSize(5).allSatisfy(actual -> {
			then(actual.amount()).isEqualTo(1000L);
			then(actual.isNew()).isFalse();
		});
	}

	@Test
	void findByOrderId() {
		// given
		List<PayHistory> payHistories = fixtures
			.set(javaGetter(PayHistory::amount), 1000L)
			.sampleList(5);

		// when
		sut.saveAll(payHistories);

		then(sut.findByOrderId(940329L)).hasSize(5).allSatisfy(actual -> {
			then(actual.amount()).isEqualTo(1000L);
			then(actual.isNew()).isFalse();
		});
	}
}

