package com.navercorp.spring.data.jdbc.plus.sql.guide.test;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.Pay;
import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.PayAdmission;
import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.PayHistory;
import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.PayHistory.PayHistoryId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class PaySpec {
	static final ArbitraryBuilderCandidate<Pay> PAY_INSTANCE = createPay();

	static final ArbitraryBuilderCandidate<PayAdmission> ADMISSION_INSTANCE = createAdmission();

	static final ArbitraryBuilderCandidate<PayHistory> HISTORY_INSTANCE = createHistory();

	private static final Arbitrary<String> PAY_METHOD_TYPE_ARBITRARY =
		Arbitraries.of("CARD", "BANK", "VIRTUAL_ACCOUNT");

	private static ArbitraryBuilderCandidate<Pay> createPay() {
		return ArbitraryBuilderCandidateFactory.of(Pay.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(Pay::id))
					.size(javaGetter(Pay::payAdmissions), 1, 3)
			);
	}

	private static ArbitraryBuilderCandidate<PayAdmission> createAdmission() {
		return ArbitraryBuilderCandidateFactory.of(PayAdmission.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(PayAdmission::id))
					.setNull(javaGetter(PayAdmission::payId))
					.set(javaGetter(PayAdmission::payMethodType), PAY_METHOD_TYPE_ARBITRARY)
			);
	}

	private static ArbitraryBuilderCandidate<PayHistory> createHistory() {
		return ArbitraryBuilderCandidateFactory.of(PayHistory.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(PayHistory::id).into(PayHistoryId::payHistoryNo))
			);
	}
}
