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

package com.navercorp.spring.data.jdbc.plus.sql.support.trait;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * The interface Single value select trait.
 *
 * @author Myeonghyeon Lee
 */
public interface SingleValueSelectTrait {
	/**
	 * Gets jdbc operations.
	 *
	 * @return the jdbc operations
	 */
	NamedParameterJdbcOperations getJdbcOperations();

	/**
	 * Select single value t.
	 *
	 * @param <T>        the type parameter
	 * @param sql        the sql
	 * @param params     the params
	 * @param returnType the return type
	 * @return the t
	 */
	default <T> @Nullable T selectSingleValue(String sql, SqlParameterSource params, Class<T> returnType) {
		return this.getJdbcOperations().queryForObject(sql, params, returnType);
	}
}
