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

import org.springframework.lang.Nullable;

/**
 * The interface Unwrapper.
 * check only unwrapping container type (ignore container generics)
 *
 * @author Myeonghyeon Lee
 *
 * @param <T> the type parameter
 */
public interface Unwrapper<T> {
	/**
	 * Unwrap object.
	 *
	 * @param value the value
	 * @return the object
	 */
	@Nullable
	Object unwrap(T value);
}
