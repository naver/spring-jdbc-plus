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

package com.navercorp.spring.data.jdbc.plus.sql.parametersource;

import java.util.Objects;
import java.util.Set;

import org.springframework.data.relational.core.sql.SqlIdentifier;

import com.navercorp.spring.data.jdbc.plus.support.parametersource.MutableSqlIdentifierParameterSource;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.IterableExpandPadding;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

/**
 * The type Convertible sql identifier parameter source.
 *
 * @author Myeonghyeon Lee
 */
class ConvertibleSqlIdentifierParameterSource implements MutableSqlIdentifierParameterSource {
	private final MutableSqlIdentifierParameterSource delegate;
	private final JdbcParameterSourceConverter converter;
	private final FallbackParameterSource fallbackParameterSource;

	private boolean padArray = false;
	private boolean paddingIterableParams = false;
	private int[] paddingIterableBoundaries = null;

	/**
	 * Instantiates a new Convertible sql identifier parameter source.
	 *
	 * @param converter               the converter
	 * @param fallbackParameterSource the fallback parameter source
	 */
	ConvertibleSqlIdentifierParameterSource(
		JdbcParameterSourceConverter converter,
		FallbackParameterSource fallbackParameterSource
	) {
		this.delegate = MutableSqlIdentifierParameterSource.create();
		this.converter = Objects.requireNonNull(converter, "Converter must not be null.");
		this.fallbackParameterSource = fallbackParameterSource;
	}

	@Override
	public Object getValue(String paramName) throws IllegalArgumentException {
		Object value = null;
		try {
			value = delegate.getValue(paramName);
		} catch (IllegalArgumentException e) {
			if (!this.isFallback(paramName)) {
				throw e;
			}
		}

		if (value == null && this.isFallback(paramName)) {
			value = this.fallbackParameterSource.fallback(paramName);
		}

		value = this.converter.convert(paramName, value);
		if (this.paddingIterableParams) {
			value = IterableExpandPadding.expandIfIterable(
				value,
				this.padArray,
				this.paddingIterableBoundaries
			);
		}

		return value;
	}

	@Override
	public Set<SqlIdentifier> getIdentifiers() {
		return delegate.getIdentifiers();
	}

	@Override
	public void addValue(SqlIdentifier name, Object value) {
		delegate.addValue(name, value);
	}

	@Override
	public void addValue(SqlIdentifier identifier, Object value, int sqlType) {
		delegate.addValue(identifier, value, sqlType);
	}

	@Override
	public void addAll(MutableSqlIdentifierParameterSource others) {
		delegate.addAll(others);
	}

	@Override
	public boolean hasValue(String paramName) {
		return delegate.hasValue(paramName);
	}

	@Override
	public int getSqlType(String paramName) {
		return delegate.getSqlType(paramName);
	}

	@Override
	public String getTypeName(String paramName) {
		return delegate.getTypeName(paramName);
	}

	@Override
	public String[] getParameterNames() {
		return delegate.getParameterNames();
	}

	/**
	 * Sets padding iterable boundaries.
	 *
	 * @param setPaddingIterableBoundaries the set padding iterable boundaries
	 */
	public void setPaddingIterableBoundaries(int[] setPaddingIterableBoundaries) {
		this.paddingIterableBoundaries = setPaddingIterableBoundaries;
	}

	/**
	 * Sets padding iterable param.
	 *
	 * @param padding the padding
	 */
	public void setPaddingIterableParam(boolean padding) {
		this.paddingIterableParams = padding;
	}

	private boolean isFallback(String paramName) {
		return this.fallbackParameterSource != null && this.fallbackParameterSource.isFallback(paramName);
	}
}
