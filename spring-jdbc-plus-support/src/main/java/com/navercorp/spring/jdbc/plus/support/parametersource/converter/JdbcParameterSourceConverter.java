package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import java.util.function.BiFunction;

@FunctionalInterface
public interface JdbcParameterSourceConverter extends BiFunction<String, Object, Object> {
	@Override
	default Object apply(String paramName, Object value) {
		return convert(paramName, value);
	}

	Object convert(String paramName, Object value);
}
