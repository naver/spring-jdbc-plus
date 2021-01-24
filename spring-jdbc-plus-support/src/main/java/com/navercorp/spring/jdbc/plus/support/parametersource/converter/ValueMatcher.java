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

/**
 * The interface ConditionalMatcher.
 * provide custom match functionality to make converters applied by custom matching logic.
 *
 * @author JunHo Yoon
 */
public interface ValueMatcher {
	/**
	 *
	 * Evaluates if provided <var>value</var> is able to handled by this or not.
	 * @param value the value which will be converted
	 * @return true if matched, false otherwise
	 */
	boolean matches(Object value);
}
