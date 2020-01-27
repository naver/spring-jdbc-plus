/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
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

package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.Java8TimeParameterTypeConverter.InstantParameterTypeConverter;

/**
 * @author Myeonghyeon Lee
 */
class DefaultJdbcParameterSourceConverterTest {
	@Test
	@SuppressWarnings("unchecked")
	void convert() {
		// given
		DefaultJdbcParameterSourceConverter sut = new DefaultJdbcParameterSourceConverter(
			Collections.singletonList(new StringTypeConverter()));
		String paramName = "name";

		// when
		Object str = sut.convert(paramName, "sample");
		Object instant = sut.convert(paramName, Instant.now());
		Object localDateTime = sut.convert(paramName, LocalDateTime.now());
		Object localDate = sut.convert(paramName, LocalDate.now());
		Object zonedDateTime = sut.convert(paramName, LocalDate.now());
		Object enumName = sut.convert(paramName, Season.SPRING);
		Object uuid = sut.convert(paramName, UUID.randomUUID());

		List<String> collections = (List<String>)sut.convert(paramName, Arrays.asList("1", "2", "3", "4", "5"));
		List<String> arrays = (List<String>)sut.convert(paramName, new String[] {"1", "2", "3", "4", "5"});

		// then
		assertThat(str).isEqualTo(":sample:");
		assertThat(instant).isExactlyInstanceOf(Date.class);
		assertThat(localDateTime).isExactlyInstanceOf(Date.class);
		assertThat(localDate).isExactlyInstanceOf(Date.class);
		assertThat(zonedDateTime).isExactlyInstanceOf(Date.class);
		assertThat(enumName).isExactlyInstanceOf(String.class);
		assertThat(uuid).isExactlyInstanceOf(String.class);

		assertThat(collections.get(0)).isEqualTo(":1:");
		assertThat(collections.get(1)).isEqualTo(":2:");
		assertThat(collections.get(2)).isEqualTo(":3:");
		assertThat(collections.get(3)).isEqualTo(":4:");
		assertThat(collections.get(4)).isEqualTo(":5:");

		assertThat(arrays.get(0)).isEqualTo(":1:");
		assertThat(arrays.get(1)).isEqualTo(":2:");
		assertThat(arrays.get(2)).isEqualTo(":3:");
		assertThat(arrays.get(3)).isEqualTo(":4:");
		assertThat(arrays.get(4)).isEqualTo(":5:");
	}

	@Test
	@DisplayName("converter 에 null 을 넣으면, null 을 반환한다. ")
	void convertNullValue() {
		// given
		DefaultJdbcParameterSourceConverter sut = new DefaultJdbcParameterSourceConverter(
			Java8TimeParameterTypeConverter.getConvertersToRegister());
		String paramName = "name";
		Instant value = null;

		// when
		Object actual = sut.convert(paramName, value);

		// then
		assertThat(actual).isNull();
	}

	@Test
	@DisplayName("converter 에 등록되지 않은 타입을 넣으면, 값을 그대로 반환한다.")
	void convertUnregisteredTypeValue() {
		// given
		DefaultJdbcParameterSourceConverter sut = new DefaultJdbcParameterSourceConverter(
			Java8TimeParameterTypeConverter.getConvertersToRegister());
		String paramName = "name";
		String value = "sample";

		// when
		Object actual = sut.convert(paramName, value);

		// then
		assertThat(actual).isEqualTo(value);
	}

	@Test
	@DisplayName("특정 ENUM 타입 Converter 를 등록하면 해당 타입에 대한 컨버터가 정상 동작한다.")
	void convertSpecificEnumConverterValue() {
		// given
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(new SportsTypeConverter());
		DefaultJdbcParameterSourceConverter sut = new DefaultJdbcParameterSourceConverter(converters);

		// when
		Object actual = sut.convert("name", Sports.SOCCER);
		Object baseEnum = sut.convert("name", Season.SUMMER);

		// then
		assertThat(actual).isExactlyInstanceOf(Integer.class);
		assertThat(actual).isEqualTo(0);
		assertThat(baseEnum).isExactlyInstanceOf(String.class);
		assertThat(baseEnum).isEqualTo("SUMMER");
	}

	@Test
	@DisplayName("Uuid 타입 컨버터를 변경한다.")
	void convertStringUuidConverterValue() {
		// given
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(UuidParameterTypeConverter.UuidToStringTypeConverter.INSTANCE);
		DefaultJdbcParameterSourceConverter sut = new DefaultJdbcParameterSourceConverter(converters);
		UUID source = UUID.randomUUID();

		// when
		Object actual = sut.convert("name", source);

		// then
		assertThat(actual).isExactlyInstanceOf(String.class);
		assertThat(actual).isEqualTo(source.toString());
	}

	@Test
	@DisplayName("Unwrapper 가 등록되어 있다면, Unwrapping 실행 후 Convert 합니다.")
	void convertWithUnwrapper() {
		// given
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(UuidParameterTypeConverter.UuidToStringTypeConverter.INSTANCE);

		List<Unwrapper<?>> unwrappers = new ArrayList<>();
		unwrappers.add(new OptionalUnwrapper());
		DefaultJdbcParameterSourceConverter sut =
			new DefaultJdbcParameterSourceConverter(converters, unwrappers);
		Optional<UUID> source = Optional.of(UUID.randomUUID());

		// when
		Object actual = sut.convert("name", source);

		// then
		assertThat(actual).isExactlyInstanceOf(String.class);
		assertThat(actual).isEqualTo(source.get().toString());
	}

	@Test
	void constructorDuplicatedConverter() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(InstantParameterTypeConverter.INSTANCE);
		converters.add(new InstantToStringTypeConverter());
		assertThatThrownBy(() -> new DefaultJdbcParameterSourceConverter(converters))
			.isExactlyInstanceOf(ParameterTypeConverterResolveException.class)
			.hasMessageContaining("duplicated")
			.hasMessageContaining(InstantParameterTypeConverter.class.getName())
			.hasMessageContaining(InstantToStringTypeConverter.class.getName());
	}

	@Test
	void constructorObjectConverter() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(InstantParameterTypeConverter.INSTANCE);
		converters.add(new ObjectTypeConverter());
		assertThatThrownBy(() -> new DefaultJdbcParameterSourceConverter(converters))
			.isExactlyInstanceOf(ParameterTypeConverterResolveException.class)
			.hasMessageContaining("Could not know")
			.hasMessageContaining(ObjectTypeConverter.class.getName());
	}

	@Test
	void constructorIterableConverter() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(InstantParameterTypeConverter.INSTANCE);
		converters.add(new IterableTypeConverter());
		assertThatThrownBy(() -> new DefaultJdbcParameterSourceConverter(converters))
			.isExactlyInstanceOf(ParameterTypeConverterResolveException.class)
			.hasMessageContaining("Iterable")
			.hasMessageContaining(IterableTypeConverter.class.getName());
	}

	@Test
	void constructorArrayConverter() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.add(InstantParameterTypeConverter.INSTANCE);
		converters.add(new ArrayTypeConverter());
		assertThatThrownBy(() -> new DefaultJdbcParameterSourceConverter(converters))
			.isExactlyInstanceOf(ParameterTypeConverterResolveException.class)
			.hasMessageContaining("Array")
			.hasMessageContaining(ArrayTypeConverter.class.getName());

	}

	private enum Season {
		SPRING, SUMMER, AUTUMN, WINTER
	}

	private enum Sports {
		SOCCER, BASE_BALL, BASKET_BALL
	}

	private static class InstantToStringTypeConverter implements Converter<Instant, String> {
		@Override
		public String convert(Instant source) {
			return source.toString();
		}
	}

	private static class StringTypeConverter implements Converter<String, String> {

		@Override
		public String convert(String source) {
			return ":" + source + ":";
		}
	}

	private static class ObjectTypeConverter implements Converter<Object, Object> {
		@Override
		public Object convert(Object source) {
			return null;
		}
	}

	private static class IterableTypeConverter implements Converter<Iterable, Iterable> {
		@Override
		public Iterable convert(Iterable source) {
			return null;
		}
	}

	private static class ArrayTypeConverter implements Converter<String[], String[]> {
		@Override
		public String[] convert(String[] source) {
			return new String[0];
		}
	}

	private static class SportsTypeConverter implements Converter<Sports, Integer> {
		@Override
		public Integer convert(Sports source) {
			return source.ordinal();
		}
	}

	private static class OptionalUnwrapper implements Unwrapper<Optional> {
		@Override
		public Object unwrap(Optional source) {
			return source.orElse(null);
		}
	}
}
