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

import java.nio.ByteBuffer;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;

/**
 * The type Uuid parameter type converter.
 *
 * @author Myeonghyeon Lee
 */
public class UuidParameterTypeConverter {
	/**
	 * The enum Uuid to byte type converter.
	 */
	public enum UuidToByteTypeConverter implements Converter<UUID, byte[]> {
		/**
		 * Instance uuid to byte type converter.
		 */
		INSTANCE;

		@Override
		public byte[] convert(UUID source) {
			ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
			bb.putLong(source.getMostSignificantBits());
			bb.putLong(source.getLeastSignificantBits());
			return bb.array();
		}
	}

	/**
	 * The enum Uuid to string type converter.
	 */
	public enum UuidToStringTypeConverter implements Converter<UUID, String> {
		/**
		 * Instance uuid to string type converter.
		 */
		INSTANCE;

		@Override
		public String convert(UUID source) {
			return source.toString();
		}
	}
}
