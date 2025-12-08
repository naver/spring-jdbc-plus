package com.navercorp.spring.data.jdbc.plus.sql.guide.test;

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.BigDecimalArbitrary;
import net.jqwik.api.arbitraries.LongArbitrary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderGroup;
import com.navercorp.fixturemonkey.jackson.introspector.JacksonObjectArbitraryIntrospector;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateList;

/**
 * Shared FixtureMonkey configuration for tests
 */
@UtilityClass
public class ArbitrarySpec {
	public static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(JacksonObjectArbitraryIntrospector.INSTANCE)
		.plugin(new JacksonPlugin())
		.plugin(new JakartaValidationPlugin())
		.plugin(
			new JqwikPlugin()
				.javaTypeArbitraryGenerator(CustomJavaTypeArbitraryGenerator.INSTANCE)
		)
		.defaultNotNull(true)
		.registerGroup(ArbitrarySpecs.INSTANCE)
		.build();

	static final Arbitrary<@NonNull Long> MONEY_ARBITRARY = Arbitraries.longs().between(10_000L, 100_000L);

	static Arbitrary<Long> UNIQUE_LONG = Arbitraries.create(System::nanoTime);

	static Arbitrary<String> UNIQUE_ID = Arbitraries.create(System::nanoTime).map(String::valueOf);

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	private static class ArbitrarySpecs implements ArbitraryBuilderGroup {
		private static final ArbitrarySpecs INSTANCE = new ArbitrarySpecs();

		@Override
		public ArbitraryBuilderCandidateList generateCandidateList() {
			return ArbitraryBuilderCandidateList.create()
				.add(PaySpec.ADMISSION_INSTANCE)
				.add(PaySpec.PAY_INSTANCE)
				.add(PaySpec.HISTORY_INSTANCE)
				.add(OrderSpec.ORDER_INSTANCE)
				.add(BoardSpec.BOARD_INSTANCE)
				.add(BoardSpec.LABEL_INSTANCE)
				.add(BoardSpec.LABEL_ID_INSTANCE)
				.add(BoardSpec.POST_INSTANCE)
				.add(BoardSpec.TAG_INSTANCE)
				.add(BoardSpec.COMMENT_INSTANCE)
				.add(BoardSpec.AUDIT_INSTANCE)
				.add(BoardSpec.AUDIT_SECRET_INSTANCE)
				.add(BoardSpec.MEMO_INSTANCE);
		}
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	private static class CustomJavaTypeArbitraryGenerator implements JavaTypeArbitraryGenerator {
		private static final CustomJavaTypeArbitraryGenerator INSTANCE = new CustomJavaTypeArbitraryGenerator();

		@Override
		public BigDecimalArbitrary bigDecimals() {
			return Arbitraries.bigDecimals()
				.between(BigDecimal.valueOf(10_000L), BigDecimal.valueOf(100_000_000L));
		}

		@Override
		public LongArbitrary longs() {
			return Arbitraries.longs()
				.between(10_000L, 100_000L);
		}
	}
}

