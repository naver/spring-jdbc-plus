package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import static java.time.ZoneId.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

public class Java8TimeParameterTypeConverter {
	public static List<Converter<?, ?>> getConvertersToRegister() {
		return Arrays.asList(
			InstantParameterTypeConverter.INSTANCE,
			LocalDateTimeParameterTypeConverter.INSTANCE,
			LocalDateParameterTypeConverter.INSTANCE,
			ZonedDateTimeParameterTypeConverter.INSTANCE);
	}

	public enum InstantParameterTypeConverter implements Converter<Instant, Date> {
		INSTANCE;

		@Override
		public Date convert(Instant source) {
			return Date.from(source.atZone(systemDefault()).toInstant());
		}
	}

	public enum LocalDateTimeParameterTypeConverter implements Converter<LocalDateTime, Date> {

		INSTANCE;

		@Override
		public Date convert(LocalDateTime source) {
			return Date.from(source.atZone(systemDefault()).toInstant());
		}
	}

	public enum LocalDateParameterTypeConverter implements Converter<LocalDate, Date> {
		INSTANCE;

		@Override
		public Date convert(LocalDate source) {
			return Date.from(source.atStartOfDay(systemDefault()).toInstant());
		}
	}

	public enum ZonedDateTimeParameterTypeConverter implements Converter<ZonedDateTime, Date> {

		INSTANCE;

		@Override
		public Date convert(ZonedDateTime source) {
			return Date.from(source.toInstant());
		}
	}
}
