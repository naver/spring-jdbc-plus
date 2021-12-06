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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * The type JSR 310 Timestamp based converters.
 *
 * @author Myeonghyeon Lee
 */
@Deprecated
public abstract class Jsr310TimestampBasedConverters {

	/**
	 * Returns the converters to be registered.
	 *
	 * Note that the {@link LocalDateTimeToTimestampConverter} is not included, since many database don't need that conversion.
	 * Databases that do need it, should include it in the conversions offered by their respective dialect.
	 *
	 * @return the converters to register
	 */
	public static List<Converter<?, ?>> getConvertersToRegister() {
		return Arrays.asList(
			LocalDateToTimestampConverter.INSTANCE,
			LocalTimeToTimestampConverter.INSTANCE,
			InstantToTimestampConverter.INSTANCE,
			ZonedDateTimeToTimestampConverter.INSTANCE
		);
	}

	public enum LocalDateTimeToTimestampConverter implements Converter<LocalDateTime, Timestamp> {

		INSTANCE;

		@NonNull
		@Override
		public Timestamp convert(LocalDateTime source) {
			return Timestamp.from(source.atZone(systemDefault()).toInstant());
		}
	}

	public enum LocalDateToTimestampConverter implements Converter<LocalDate, Timestamp> {

		INSTANCE;

		@NonNull
		@Override
		public Timestamp convert(LocalDate source) {
			return Timestamp.from(source.atStartOfDay(systemDefault()).toInstant());
		}
	}

	public enum LocalTimeToTimestampConverter implements Converter<LocalTime, Timestamp> {

		INSTANCE;

		@NonNull
		@Override
		public Timestamp convert(LocalTime source) {
			return Timestamp.from(source.atDate(LocalDate.now()).atZone(systemDefault()).toInstant());
		}
	}

	public enum InstantToTimestampConverter implements Converter<Instant, Timestamp> {

		INSTANCE;

		@NonNull
		@Override
		public Timestamp convert(Instant source) {
			return Timestamp.from(source);
		}
	}

	public enum ZonedDateTimeToTimestampConverter implements Converter<ZonedDateTime, Timestamp> {

		INSTANCE;

		@NonNull
		@Override
		public Timestamp convert(ZonedDateTime source) {
			return Timestamp.from(source.toInstant());
		}
	}
}
