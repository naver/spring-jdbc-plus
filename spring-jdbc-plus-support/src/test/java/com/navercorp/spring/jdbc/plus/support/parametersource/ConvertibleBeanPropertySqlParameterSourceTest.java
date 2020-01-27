package com.navercorp.spring.jdbc.plus.support.parametersource;

import static org.assertj.core.api.Assertions.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.Java8TimeParameterTypeConverter;

class ConvertibleBeanPropertySqlParameterSourceTest {
	private final JdbcParameterSourceConverter converter = new DefaultJdbcParameterSourceConverter(
		Collections.singletonList(Java8TimeParameterTypeConverter.InstantParameterTypeConverter.INSTANCE));

	@Test
	@DisplayName("생성자에 Converter 로 null 을 넘기면 NullPointerException 이 발생합니다.")
	void constructorNullConverter() {
		Criteria criteria = Criteria.of("sample", Instant.now());
		assertThatThrownBy(() -> new ConvertibleBeanPropertySqlParameterSource(criteria, null))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("Converter must not be null");
	}

	@Test
	void getValue() {
		// given
		String paramName = "occurrenceTime";
		Criteria criteria = Criteria.of("sample", Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			criteria, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isNotNull();
		assertThat(actual).isExactlyInstanceOf(Date.class);
		assertThat(actual).isEqualTo(Java8TimeParameterTypeConverter.InstantParameterTypeConverter.INSTANCE.convert(criteria.getOccurrenceTime()));
	}

	@Test
	@DisplayName("value 타입의 Converter 가 등록되지 않았다면, 그대로 반환합니다.")
	void getValueNotRegisteredType() {
		// given
		String paramName = "name";
		Criteria criteria = Criteria.of("sample", Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			criteria, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isEqualTo("sample");
	}

	@Test
	@DisplayName("Object 의 value 가 null 이면, null 을 반환합니다.")
	void getValueHasParamButNullValue() {
		// given
		String paramName = "name";
		Criteria criteria = Criteria.of(null, Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			criteria, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isNull();
	}

	@Test
	@DisplayName("Object 의 field 가 존재하지 않으면, IllegalArgumentException 이 발생합니다.")
	void getValueNoParam() {
		String paramName = "not-exist";
		Criteria criteria = Criteria.of(null, Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			criteria, this.converter);

		assertThatThrownBy(() -> sut.getValue(paramName))
			.isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Object 의 field 가 존재하지 않지만, Fallback 파라미터라면 Exception 이 발생하지 않습니다.")
	void getValueNoParamButFallback() {
		// given
		String paramName = "none";
		Criteria criteria = Criteria.of(null, Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			criteria, this.converter, new TestFallbackParamSource());

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isEqualTo("fallback");
	}

	@Test
	@DisplayName("Iterable 한 값은, element 를 컨버팅한 후 expand padding 을 수행한다.")
	@SuppressWarnings("unchecked")
	void getValueIterablePadding() {
		// given
		String name = "sample";
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		CriteriaIn criteria = CriteriaIn.of(name, value);
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(criteria, this.converter);
		sut.setPaddingIterableParam(true);

		// when
		Object actual = sut.getValue("list");

		// then
		assertThat(actual).isInstanceOf(List.class);
		List<Timestamp> list = (List<Timestamp>)actual;
		assertThat(list).hasSize(8);
		assertThat(list.get(0)).isEqualTo(Java8TimeParameterTypeConverter.InstantParameterTypeConverter.INSTANCE.convert(value.get(0)));
		assertThat(list.get(4)).isEqualTo(Java8TimeParameterTypeConverter.InstantParameterTypeConverter.INSTANCE.convert(value.get(4)));
		assertThat(list.get(7)).isEqualTo(Java8TimeParameterTypeConverter.InstantParameterTypeConverter.INSTANCE.convert(value.get(4)));
	}

	@Test
	@DisplayName("iterablePaddingBoundaries 를 주입하더라도 paddingIterableParam 이 false 면 padding 하지 않는다.")
	@SuppressWarnings("unchecked")
	void getValueIterablePaddingBoundariesButFalse() {
		// given
		String name = "sample";
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		CriteriaIn criteria = CriteriaIn.of(name, value);
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(criteria, this.converter);
		sut.setPaddingIterableBoundaries(new int[] {1, 10});
		sut.setPaddingIterableParam(false);

		// when
		Object actual = sut.getValue("list");

		// then
		assertThat(actual).isInstanceOf(List.class);
		List<Timestamp> list = (List<Timestamp>)actual;
		assertThat(list).hasSize(5);
	}

	static class Criteria {
		private String name;
		private Instant occurrenceTime;

		static Criteria of(String name, Instant occurrenceTime) {
			Criteria criteria = new Criteria();
			criteria.name = name;
			criteria.occurrenceTime = occurrenceTime;
			return criteria;
		}

		public String getName() {
			return this.name;
		}

		public Instant getOccurrenceTime() {
			return this.occurrenceTime;
		}
	}

	static class CriteriaIn {
		private String name;
		private List<Instant> list;

		static CriteriaIn of(String name, List<Instant> list) {
			CriteriaIn criteria = new CriteriaIn();
			criteria.name = name;
			criteria.list = list;
			return criteria;
		}

		public String getName() {
			return this.name;
		}

		public List<Instant> getList() {
			return this.list;
		}
	}

	static class TestFallbackParamSource implements FallbackParameterSource {
		@Override
		public boolean isFallback(String paramName) {
			return paramName.equals("none");
		}

		@Override
		public Object fallback(String paramName) {
			return "fallback";
		}
	}
}
