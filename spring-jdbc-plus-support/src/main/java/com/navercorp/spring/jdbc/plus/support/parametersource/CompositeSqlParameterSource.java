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

package com.navercorp.spring.jdbc.plus.support.parametersource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * The type Composite sql parameter source.
 *
 * @author Myeonghyeon Lee
 */
public class CompositeSqlParameterSource implements SqlParameterSource {
	private final List<SqlParameterSource> sqlParameterSources;

	/**
	 * Instantiates a new Composite sql parameter source.
	 *
	 * @param sqlParameterSources the sql parameter sources
	 */
	public CompositeSqlParameterSource(SqlParameterSource... sqlParameterSources) {
		this.sqlParameterSources = Arrays.asList(sqlParameterSources);
	}

	/**
	 * Instantiates a new Composite sql parameter source.
	 *
	 * @param sqlParameterSources the sql parameter sources
	 */
	public CompositeSqlParameterSource(List<SqlParameterSource> sqlParameterSources) {
		this.sqlParameterSources = Collections.unmodifiableList(sqlParameterSources);
	}

	@Override
	public boolean hasValue(String paramName) {
		for (SqlParameterSource each : this.sqlParameterSources) {
			if (each.hasValue(paramName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public @Nullable Object getValue(String paramName) throws IllegalArgumentException {
		for (SqlParameterSource each : this.sqlParameterSources) {
			if (each.hasValue(paramName)) {
				return each.getValue(paramName);
			}
		}
		throw new IllegalArgumentException(
			"Can not find '" + paramName + "' parameter in CompositeSqlParameterSource.");
	}

	@Override
	public int getSqlType(String paramName) {
		for (SqlParameterSource each : this.sqlParameterSources) {
			if (each.hasValue(paramName)) {
				return each.getSqlType(paramName);
			}
		}
		return TYPE_UNKNOWN;
	}

	@Nullable
	@Override
	public String getTypeName(String paramName) {
		for (SqlParameterSource each : this.sqlParameterSources) {
			if (each.hasValue(paramName)) {
				return each.getTypeName(paramName);
			}
		}
		return null;
	}

	@Override
	public String[] getParameterNames() {
		return this.sqlParameterSources.stream()
			.map(SqlParameterSource::getParameterNames)
			.flatMap(Arrays::stream)
			.toArray(String[]::new);
	}

	@Override
	public String toString() {
		return "CompositeSqlParameterSource{"
			+ "sqlParameterSources=" + sqlParameterSources
			+ '}';
	}
}
