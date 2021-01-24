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

/**
 * The type Java 8 time parameter type converter.
 *
 * @author Myeonghyeon Lee
 */
public class Java8TimeParameterTypeConverter {
	/**
	 * Gets converters to register.
	 *
	 * @return the converters to register
	 */
	public static List<Converter<?, ?>> getConvertersToRegister() {
		return Arrays.asList(
			InstantParameterTypeConverter.INSTANCE,
			LocalDateTimeParameterTypeConverter.INSTANCE,
			LocalDateParameterTypeConverter.INSTANCE,
			ZonedDateTimeParameterTypeConverter.INSTANCE);
	}

	/**
	 * The enum Instant parameter type converter.
	 */
	public enum InstantParameterTypeConverter implements Converter<Instant, Date> {
		/**
		 * Instance instant parameter type converter.
		 */
		INSTANCE;

		@Override
		public Date convert(Instant source) {
			return Date.from(source.atZone(systemDefault()).toInstant());
		}
	}

	/**
	 * The enum Local date time parameter type converter.
	 */
	public enum LocalDateTimeParameterTypeConverter implements Converter<LocalDateTime, Date> {

		/**
		 * Instance local date time parameter type converter.
		 */
		INSTANCE;

		@Override
		public Date convert(LocalDateTime source) {
			return Date.from(source.atZone(systemDefault()).toInstant());
		}
	}

	/**
	 * The enum Local date parameter type converter.
	 */
	public enum LocalDateParameterTypeConverter implements Converter<LocalDate, Date> {
		/**
		 * Instance local date parameter type converter.
		 */
		INSTANCE;

		@Override
		public Date convert(LocalDate source) {
			return Date.from(source.atStartOfDay(systemDefault()).toInstant());
		}
	}

	/**
	 * The enum Zoned date time parameter type converter.
	 */
	public enum ZonedDateTimeParameterTypeConverter implements Converter<ZonedDateTime, Date> {

		/**
		 * Instance zoned date time parameter type converter.
		 */
		INSTANCE;

		@Override
		public Date convert(ZonedDateTime source) {
			return Date.from(source.toInstant());
		}
	}
}
