package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import org.springframework.core.convert.converter.Converter;

public class EnumParameterTypeConverter {
	public enum EnumToNameConverter implements Converter<Enum, String> {
		INSTANCE;

		@Override
		public String convert(Enum source) {
			return source.name();
		}
	}

	public enum EnumToOrdinalConverter implements Converter<Enum, Integer> {
		INSTANCE;

		@Override
		public Integer convert(Enum source) {
			return source.ordinal();
		}
	}
}
