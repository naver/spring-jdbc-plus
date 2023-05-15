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

import static org.assertj.core.api.Assertions.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.javaunit.autoparams.AutoSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.Jsr310TimestampBasedConverters.InstantToTimestampConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

/**
 * @author Myeonghyeon Lee
 */
class ConvertibleMapSqlParameterSourceTest {
	private final JdbcParameterSourceConverter converter = new DefaultJdbcParameterSourceConverter(
		Collections.singletonList(InstantToTimestampConverter.INSTANCE));

	@Test
	@DisplayName("생성자에 Converter 로 null 을 넘기면 NullPointerException 이 발생합니다.")
	void constructorNullConverter() {
		Map<String, Instant> map = Collections.singletonMap("sample", Instant.now());
		assertThatThrownBy(() -> new ConvertibleMapSqlParameterSource(map, null))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("Converter must not be null");
	}

	@ParameterizedTest
	@AutoSource
	void getValue(String paramName) {
		// given
		Instant value = Instant.now();
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isNotNull();
		assertThat(actual).isExactlyInstanceOf(Timestamp.class);
		assertThat(actual).isEqualTo(
			InstantToTimestampConverter.INSTANCE.convert(value));
	}

	@DisplayName("value 타입의 Converter 가 등록되지 않았다면, 그대로 반환합니다.")
	@ParameterizedTest
	@AutoSource
	void getValueNotRegisteredType(String paramName, String value) {
		// given
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isEqualTo(value);
	}

	@DisplayName("Map 의 value 가 null 이면, null 을 반환합니다.")
	@ParameterizedTest
	@AutoSource
	void getValueHasParamButNullValue(String paramName) {
		// given
		Instant value = null;
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isNull();
	}

	@DisplayName("Map 의 key 가 존재하지 않으면, IllegalArgumentException 이 발생합니다.")
	@ParameterizedTest
	@AutoSource
	void getValueNoParam(String paramName) {
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

	@DisplayName("Iterable 한 값은, element 를 컨버팅한 후 expand padding 을 수행한다.")
	@ParameterizedTest
	@AutoSource
	@SuppressWarnings("unchecked")
	void getValueIterablePadding(String paramName) {
		// given
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);
		sut.setPaddingIterableParam(true);
		sut.setPadArray(true);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isInstanceOf(List.class);
		List<Timestamp> list = (List<Timestamp>)actual;
		assertThat(list).hasSize(8);
		assertThat(list.get(0)).isEqualTo(
			InstantToTimestampConverter.INSTANCE.convert(value.get(0)));
		assertThat(list.get(4)).isEqualTo(
			InstantToTimestampConverter.INSTANCE.convert(value.get(4)));
		assertThat(list.get(7)).isEqualTo(
			InstantToTimestampConverter.INSTANCE.convert(value.get(4)));
	}

	@DisplayName("Array 는 expand padding flag 가 false 면 padding 을 수행하지 않는다.")
	@ParameterizedTest
	@AutoSource
	@SuppressWarnings("unchecked")
	void getValueIterablePaddingForArray(String paramName) {
		// given
		Instant now = Instant.now();
		Instant[] value = {now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60)};
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);
		sut.setPaddingIterableParam(true);
		sut.setPadArray(false);

		// when
		Object actual = sut.getValue(paramName);

		// then
		assertThat(actual).isInstanceOf(Object[].class);
		Object[] array = (Object[])actual;
		assertThat(array).hasSize(5);
		assertThat(array[0]).isEqualTo(
			InstantToTimestampConverter.INSTANCE.convert(value[0]));
		assertThat(array[4]).isEqualTo(
			InstantToTimestampConverter.INSTANCE.convert(value[4]));
	}

	@DisplayName("iterablePaddingBoundaries 를 주입하더라도 paddingIterableParam 이 false 면 padding 하지 않는다.")
	@ParameterizedTest
	@AutoSource
	@SuppressWarnings("unchecked")
	void getValueIterablePaddingBoundariesButFalse(String paramName) {
		// given
		Instant now = Instant.now();
		List<Instant> value = Arrays.asList(now.minusSeconds(300), now.minusSeconds(240),
			now.minusSeconds(180), now.minusSeconds(120), now.minusSeconds(60));
		Map<String, Object> map = Collections.singletonMap(paramName, value);
		ConvertibleMapSqlParameterSource sut = new ConvertibleMapSqlParameterSource(map, this.converter);
		sut.setPaddingIterableBoundaries(new int[] {1, 10});
		sut.setPaddingIterableParam(false);
		sut.setPadArray(true);

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
