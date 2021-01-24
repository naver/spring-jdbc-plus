/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
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

import static com.navercorp.spring.jdbc.plus.support.parametersource.converter.Java8TimeParameterTypeConverter.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

/**
 * @author Myeonghyeon Lee
 */
class ConvertibleMapSqlParameterSourceTest {
	private final JdbcParameterSourceConverter converter = new DefaultJdbcParameterSourceConverter(
		Collections.singletonList(InstantParameterTypeConverter.INSTANCE));

	@Test
	@DisplayName("생성자에 Converter 로 null 을 넘기면 NullPointerException 이 발생합니다.")
	void constructorNullConverter() {
		Map<String, Instant> map = Collections.singletonMap("sample", Instant.now());
		assertThatThrownBy(() -> new ConvertibleMapSqlParameterSource(map, null))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("Converter must not be null");
	}

	@Test
	void getValue() {
		// given
		String paramName = "paramName";
		Instant value = Instant.now();
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isNotNull();
		assertThat(actual).isExactlyInstanceOf(Date.class);
		assertThat(actual).isEqualTo(
			InstantParameterTypeConverter.INSTANCE.convert(value));
	}

	@Test
	@DisplayName("value 타입의 Converter 가 등록되지 않았다면, 그대로 반환합니다.")
	void getValueNotRegisteredType() {
		// given
		String paramName = "paramName";
		String value = "not-registered-name";
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isEqualTo(value);
	}

	@Test
	@DisplayName("Map 의 value 가 null 이면, null 을 반환합니다.")
	void getValueHasParamButNullValue() {
		// given
		String paramName = "paramName";
		Instant value = null;
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isNull();
	}

	@Test
	@DisplayName("Map 의 key 가 존재하지 않으면, IllegalArgumentException 이 발생합니다.")
	void getValueNoParam() {
		String paramName = "paramName";
		Instant value = null;
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);

		assertThatThrownBy(() -> sut.getValue("not-exist"))
			.isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Object 의 field 가 존재하지 않지만, Fallback 파라미터라면 Exception 이 발생하지 않습니다.")
	void getValueNoParamButFallback() {
		// given
		String paramName = "none";
		Instant value = null;
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(
			map, this.converter, new TestFallbackParamSource());

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
		String paramName = "paramName";
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);
		sut.setPaddingIterableParam(true);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isInstanceOf(List.class);
		List<Timestamp> list = (List<Timestamp>)actual;
		assertThat(list).hasSize(8);
		assertThat(list.get(0)).isEqualTo(
			InstantParameterTypeConverter.INSTANCE.convert(value.get(0)));
		assertThat(list.get(4)).isEqualTo(
			InstantParameterTypeConverter.INSTANCE.convert(value.get(4)));
		assertThat(list.get(7)).isEqualTo(
			InstantParameterTypeConverter.INSTANCE.convert(value.get(4)));
	}

	@Test
	@DisplayName("iterablePaddingBoundaries 를 주입하더라도 paddingIterableParam 이 false 면 padding 하지 않는다.")
	@SuppressWarnings("unchecked")
	void getValueIterablePaddingBoundariesButFalse() {
		// given
		String paramName = "paramName";
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);
		sut.setPaddingIterableBoundaries(new int[] {1, 10});
		sut.setPaddingIterableParam(false);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isInstanceOf(List.class);
		List<Timestamp> list = (List<Timestamp>)actual;
		assertThat(list).hasSize(5);
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
