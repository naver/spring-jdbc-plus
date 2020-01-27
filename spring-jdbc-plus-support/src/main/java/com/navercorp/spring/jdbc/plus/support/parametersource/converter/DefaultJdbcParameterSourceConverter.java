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

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.EnumParameterTypeConverter.EnumToNameConverter;

/**
 * The type Default jdbc parameter source converter.
 *
 * @author Myeonghyeon Lee
 */
public class DefaultJdbcParameterSourceConverter implements JdbcParameterSourceConverter {
	private final Map<Class<?>, Converter<?, ?>> converters;
	private final Converter<Enum, Object> enumConverter;
	private final Map<Class<?>, Unwrapper<?>> unwrappers;

	/**
	 * Instantiates a new Default jdbc parameter source converter.
	 */
	public DefaultJdbcParameterSourceConverter() {
		this(Collections.emptyList());
	}

	/**
	 * Instantiates a new Default jdbc parameter source converter.
	 *
	 * @param converters the converters
	 */
	public DefaultJdbcParameterSourceConverter(List<Converter<?, ?>> converters) {
		this(converters, Collections.emptyList());
	}

	/**
	 * Instantiates a new Default jdbc parameter source converter.
	 *
	 * @param converters the converters
	 * @param unwrappers the unwrappers
	 */
	@SuppressWarnings("unchecked")
	public DefaultJdbcParameterSourceConverter(List<Converter<?, ?>> converters, List<Unwrapper<?>> unwrappers) {
		this.converters = getConvertersMap(converters);
		Converter enumConverter = this.converters.get(Enum.class);
		this.enumConverter = defaultIfNull(enumConverter, EnumToNameConverter.INSTANCE);
		this.unwrappers = getUnwrappersMap(unwrappers);
	}

	private static Map<Class<?>, Converter<?, ?>> getConvertersMap(List<Converter<?, ?>> converters) {
		Map<Class<?>, Converter<?, ?>> converterMap = new HashMap<>();
		for (Converter<?, ?> converter : converters) {
			Class<?> generics = resolveConverterGenerics(converter.getClass()).get(0);
			if (generics == Object.class) {
				throw new ParameterTypeConverterResolveException(
					"Could not know Converter target type. converterType: " + converter.getClass());
			}
			if (Iterable.class.isAssignableFrom(generics)) {
				throw new ParameterTypeConverterResolveException(
					"Converter target type is Iterable. converterType: " + converter.getClass());
			}
			if (generics.isArray()) {
				throw new ParameterTypeConverterResolveException(
					"Converter target type is array. converterType: " + converter.getClass());
			}
			if (converterMap.containsKey(generics)) {
				throw new ParameterTypeConverterResolveException(
					"Converter target type duplicated. "
						+ "converter: " + converterMap.get(generics).getClass()
						+ " duplicated: " + converter.getClass()
						+ " generics: " + generics);
			}
			converterMap.put(generics, converter);
		}

		Map<Class<?>, Converter<?, ?>> result = getDefaultConverters();
		result.putAll(converterMap);
		return Collections.unmodifiableMap(result);
	}

	private static Map<Class<?>, Converter<?, ?>> getDefaultConverters() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.addAll(Java8TimeParameterTypeConverter.getConvertersToRegister());
		converters.add(UuidParameterTypeConverter.UuidToStringTypeConverter.INSTANCE);
		return converters.stream()
			.collect(toMap(c -> resolveConverterGenerics(c.getClass()).get(0), c -> c));
	}

	private static Map<Class<?>, Unwrapper<?>> getUnwrappersMap(List<Unwrapper<?>> unwrappers) {
		Map<Class<?>, Unwrapper<?>> unwrapperMap = new HashMap<>();
		for (Unwrapper<?> unwrapper : unwrappers) {
			Class<?> generics = resolveUnwrapperGenerics(unwrapper.getClass()).get(0);
			if (generics == Object.class) {
				throw new ParameterTypeConverterResolveException(
					"Could not know Unwrapper target containerType. "
						+ "containerType: " + unwrapper.getClass());
			}
			if (unwrapperMap.containsKey(generics)) {
				throw new ParameterTypeConverterResolveException(
					"Unwrapper target containerType duplicated. "
						+ "unwrapper: " + unwrapperMap.get(generics).getClass()
						+ " duplicated: " + unwrapper.getClass()
						+ " generics: " + generics);
			}
			unwrapperMap.put(generics, unwrapper);
		}

		return Collections.unmodifiableMap(unwrapperMap);
	}

	private static List<Class<?>> resolveConverterGenerics(Class<?> type) {
		return Arrays.asList(ResolvableType.forClass(Converter.class, type).resolveGenerics());
	}

	private static List<Class<?>> resolveUnwrapperGenerics(Class<?> type) {
		return Arrays.asList(ResolvableType.forClass(Unwrapper.class, type).resolveGenerics());
	}

	private static <T> T defaultIfNull(T object, T defaultValue) {
		return object != null ? object : defaultValue;
	}

	@Nullable
	@Override
	public Object convert(String paramName, Object value) {
		if (value == null) {
			return null;
		}

		return this.convert(value);
	}

	@SuppressWarnings("unchecked")
	private Object convert(Object value) {
		Unwrapper unwrapper = this.unwrappers.get(value.getClass());
		if (unwrapper != null) {
			value = unwrapper.unwrap(value);
		}

		if (value == null) {
			return null;
		}

		if (value instanceof Iterable) {
			value = this.convertElements((Iterable)value);
		} else if (value.getClass().isArray()) {
			value = this.convertElements((Object[])value);
		}

		Converter converter = this.converters.get(value.getClass());
		if (converter != null) {
			value = converter.convert(value);
		} else if (value instanceof Enum) {
			value = enumConverter.convert((Enum)value);
		}
		return value;
	}

	private List<?> convertElements(Object[] array) {
		List<Object> result = new ArrayList<>();
		for (Object element : array) {
			result.add(this.convert(element));
		}
		return result;
	}

	private List<?> convertElements(Iterable<?> iterable) {
		List<Object> result = new ArrayList<>();
		for (Object element : iterable) {
			result.add(this.convert(element));
		}
		return result;
	}
}
