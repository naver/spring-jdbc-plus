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

import org.springframework.core.convert.converter.Converter;

/**
 * The type Enum parameter type converter.
 *
 * @author Myeonghyeon Lee
 */
public class EnumParameterTypeConverter {
	/**
	 * The enum Enum to name converter.
	 */
	public enum EnumToNameConverter implements Converter<Enum, String> {
		/**
		 * Instance enum to name converter.
		 */
		INSTANCE;

		@Override
		public String convert(Enum source) {
			return source.name();
		}
	}

	/**
	 * The enum Enum to ordinal converter.
	 */
	public enum EnumToOrdinalConverter implements Converter<Enum, Integer> {
		/**
		 * Instance enum to ordinal converter.
		 */
		INSTANCE;

		@Override
		public Integer convert(Enum source) {
			return source.ordinal();
		}
	}
}
