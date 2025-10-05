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

package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * The type JSR 310 Timestamp based converters.
 * <p>
 * COPY {@link org.springframework.data.jdbc.core.convert.Jsr310TimestampBasedConverters}
 * except TimestampToLocal** converters, because of backward compatibility.
 *
 * @author Myeonghyeon Lee
 */
abstract class Jsr310TimestampBasedConverters {

	static Collection<Converter<?, ?>> getConvertersToRegister() {
		List<Converter<?, ?>> converters = new ArrayList<>(4);

		converters.add(LocalDateToTimestampConverter.INSTANCE);
		converters.add(LocalTimeToTimestampConverter.INSTANCE);
		converters.add(InstantToTimestampConverter.INSTANCE);
		converters.add(ZonedDateTimeToTimestampConverter.INSTANCE);

		return converters;
	}

	enum LocalDateToTimestampConverter implements Converter<LocalDate, Timestamp> {
		INSTANCE;

		LocalDateToTimestampConverter() {
		}

		@NonNull
		public Timestamp convert(LocalDate source) {
			return Timestamp.from(source.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
	}

	enum LocalTimeToTimestampConverter implements Converter<LocalTime, Timestamp> {
		INSTANCE;

		LocalTimeToTimestampConverter() {
		}

		@NonNull
		public Timestamp convert(LocalTime source) {
			return Timestamp.from(source.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
		}
	}

	enum InstantToTimestampConverter implements Converter<Instant, Timestamp> {
		INSTANCE;

		InstantToTimestampConverter() {
		}

		@NonNull
		public Timestamp convert(Instant source) {
			return Timestamp.from(source);
		}
	}

	enum ZonedDateTimeToTimestampConverter implements Converter<ZonedDateTime, Timestamp> {
		INSTANCE;

		ZonedDateTimeToTimestampConverter() {
		}

		@NonNull
		public Timestamp convert(ZonedDateTime source) {
			return Timestamp.from(source.toInstant());
		}
	}
}
