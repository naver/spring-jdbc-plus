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

package com.navercorp.spring.data.jdbc.plus.sql.support;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import com.navercorp.spring.data.jdbc.plus.support.convert.SqlProvider;

/**
 * The type Sql generator support.
 *
 * @author Myeonghyeon Lee
 */
// FIXME: Mark nullable
@NullUnmarked
public abstract class SqlGeneratorSupport implements SqlAware {
	/**
	 * The Sql.
	 */
	protected SqlProvider sql;

	public void setSql(@NonNull SqlProvider sql) {
		this.sql = sql;
	}
}
