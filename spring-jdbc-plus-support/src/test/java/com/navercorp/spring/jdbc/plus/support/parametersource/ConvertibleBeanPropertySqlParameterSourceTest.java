/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.jdbc.plus.support.parametersource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.javaunit.autoparams.AutoSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

/**
 * @author Myeonghyeon Lee
 * @author IAM20
 */
class ConvertibleBeanPropertySqlParameterSourceTest {
	private final JdbcParameterSourceConverter converter = new DefaultJdbcParameterSourceConverter(List.of());

	@DisplayName("생성자에 Converter 로 null 을 넘기면 NullPointerException 이 발생합니다.")
	@ParameterizedTest
	@AutoSource
	void constructorNullConverter(String name) {
		Criteria criteria = Criteria.of(name, Instant.now());
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
		assertThat(actual).isExactlyInstanceOf(Timestamp.class);
		assertThat(actual).isEqualTo(Timestamp.from(criteria.getOccurrenceTime()));
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

	@DisplayName("Object 의 field 가 존재하지 않으면, IllegalArgumentException 이 발생합니다.")
	@ParameterizedTest
	@AutoSource
	void getValueNoParam(String paramName) {
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
	void getPrefixValue() {
		// given
		Instant now = Instant.now();
		String paramName = "test.occurrenceTime";
		Criteria criteria = Criteria.of("sample", now);
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			"test.", criteria, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(sut.hasValue(paramName)).isTrue();
		assertThat(actual).isEqualTo(Timestamp.from(now));
	}

	@Test
	void getWhitespaceIncludedPrefixValue() {
		// given
		Instant now = Instant.now();
		String paramName = "t e s t .occurrenceTime";
		Criteria criteria = Criteria.of("sample", now);
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			"    t e s t .    ", criteria, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(sut.hasValue(paramName)).isTrue();
		assertThat(actual).isEqualTo(Timestamp.from(now));
	}

	@Test
	void getValuePrefixNotMathced() {
		String paramName = "occurrenceTime";
		Criteria criteria = Criteria.of("sample", Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			"test.", criteria, this.converter);

		assertThatThrownBy(() -> sut.getValue(paramName))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessage("Param name does not starts with test.");
		assertThat(sut.hasValue(paramName)).isFalse();
	}

	@DisplayName("Prefix 가 없지만 fallback parameter 라면 Exception 이 발생하지 않습니다.")
	@Test
	void getValuePrefixNotMatchedButFallback() {
		// given
		String paramName = "none";
		Criteria criteria = Criteria.of(null, Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			"test.", criteria, this.converter, new TestFallbackParamSource());

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isEqualTo("fallback");
		assertThat(sut.hasValue(paramName)).isFalse();
	}

	@DisplayName("Object의 Field 가 존재하지 않지만, Prefix 가 있는 fallback parameter 라면 Exception 이 발생하지 않습니다.")
	@Test
	void getValueNoParamButFallbackInPrefixSource() {
		// given
		String paramName = "test.none";
		Criteria criteria = Criteria.of(null, Instant.now());
		ConvertibleBeanPropertySqlParameterSource sut = new ConvertibleBeanPropertySqlParameterSource(
			"test.", criteria, this.converter, new TestFallbackParamSource());

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isEqualTo("fallback");
		assertThat(sut.hasValue(paramName)).isFalse();
	}

	@DisplayName("Iterable 한 값은, element 를 컨버팅한 후 expand padding 을 수행한다.")
	@ParameterizedTest
	@AutoSource
	@SuppressWarnings("unchecked")
	void getValueIterablePadding(String name) {
		// given
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		CriteriaIn criteria = CriteriaIn.of(name, value);
		ConvertibleBeanPropertySqlParameterSource sut =
			new ConvertibleBeanPropertySqlParameterSource(criteria, this.converter);
		sut.setPaddingIterableParam(true);

		// when
		Object actual = sut.getValue("list");

		// then
		assertThat(actual).isInstanceOf(List.class);
		List<Timestamp> list = (List<Timestamp>)actual;
		assertThat(list).hasSize(8);
		assertThat(list.get(0)).isEqualTo(Timestamp.from(value.get(0)));
		assertThat(list.get(4)).isEqualTo(Timestamp.from(value.get(4)));
		assertThat(list.get(7)).isEqualTo(Timestamp.from(value.get(4)));
	}

	@DisplayName("iterablePaddingBoundaries 를 주입하더라도 paddingIterableParam 이 false 면 padding 하지 않는다.")
	@ParameterizedTest
	@AutoSource
	@SuppressWarnings("unchecked")
	void getValueIterablePaddingBoundariesButFalse(String name) {
		// given
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		CriteriaIn criteria = CriteriaIn.of(name, value);
		ConvertibleBeanPropertySqlParameterSource sut =
			new ConvertibleBeanPropertySqlParameterSource(criteria, this.converter);
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
