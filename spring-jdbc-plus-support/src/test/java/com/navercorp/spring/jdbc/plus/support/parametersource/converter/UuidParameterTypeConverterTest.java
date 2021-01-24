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

import static org.assertj.core.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.UuidParameterTypeConverter.UuidToByteTypeConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.UuidParameterTypeConverter.UuidToStringTypeConverter;

/**
 * @author Myeonghyeon Lee
 */
class UuidParameterTypeConverterTest {
	private static UUID getUuidFromBytes(byte[] bytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		long high = byteBuffer.getLong();
		long low = byteBuffer.getLong();
		return new UUID(high, low);
	}

	@Test
	void uuidToByte() {
		// given
		UuidToByteTypeConverter sut = UuidToByteTypeConverter.INSTANCE;
		UUID source = UUID.randomUUID();

		// when
		byte[] actual = sut.convert(source);

		// then
		assertThat(getUuidFromBytes(actual)).isEqualTo(source);
	}

	@Test
	void uuidToString() {
		// given
		UuidToStringTypeConverter sut = UuidToStringTypeConverter.INSTANCE;
		UUID source = UUID.randomUUID();

		// when
		String actual = sut.convert(source);

		// then
		assertThat(actual).isEqualTo(source.toString());
		assertThat(UUID.fromString(actual)).isEqualTo(source);
	}
}
