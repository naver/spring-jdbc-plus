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

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.IterableExpandPadding;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

/**
 * The type Convertible bean property sql parameter source.
 *
 * @author Myeonghyeon Lee
 * @author IAM20
 */
public class ConvertibleBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {
	private final JdbcParameterSourceConverter converter;
	private final @Nullable FallbackParameterSource fallbackParameterSource;
	private final @Nullable String prefix;

	private boolean padArray = false;

	private boolean paddingIterableParams = false;
	private int @Nullable[] paddingIterableBoundaries = null;

	/**
	 * Instantiates a new Convertible bean property sql parameter source.
	 *
	 * @param bean      the bean
	 * @param converter the converter
	 */
	public ConvertibleBeanPropertySqlParameterSource(Object bean, JdbcParameterSourceConverter converter) {
		this(bean, converter, null);
	}

	/**
	 * Instantiates a new Convertible bean property sql parameter source.
	 *
	 * @param bean                    the bean
	 * @param converter               the converter
	 * @param fallbackParameterSource the fallback parameter source
	 */
	public ConvertibleBeanPropertySqlParameterSource(
		Object bean,
		JdbcParameterSourceConverter converter,
		@Nullable FallbackParameterSource fallbackParameterSource) {

		this(null, bean, converter, fallbackParameterSource);
	}

	/**
	 * Instantiates a new Convertible bean property sql parameter source.
	 *
	 * @param prefix                  the prefix
	 * @param bean                    the bean
	 * @param converter               the converter
	 */
	public ConvertibleBeanPropertySqlParameterSource(
		@Nullable String prefix,
		Object bean,
		JdbcParameterSourceConverter converter
	) {
		this(prefix, bean, converter, null);
	}

	/**
	 * Instantiates a new Convertible bean property sql parameter source.
	 *
	 * @param prefix                  the prefix
	 * @param bean                    the bean
	 * @param converter               the converter
	 * @param fallbackParameterSource the fallback parameter source
	 */
	public ConvertibleBeanPropertySqlParameterSource(
		@Nullable String prefix,
		Object bean,
		JdbcParameterSourceConverter converter,
		@Nullable FallbackParameterSource fallbackParameterSource
	) {

		super(bean);
		this.prefix = prefix != null ? prefix.trim() : null;
		this.converter = Objects.requireNonNull(converter, "Converter must not be null.");
		this.fallbackParameterSource = fallbackParameterSource;
	}

	@Override
	public boolean hasValue(String paramName) {
		String patchedParamName;
		try {
			patchedParamName = this.patchParamName(paramName);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return super.hasValue(patchedParamName);
	}

	@Nullable
	@Override
	public Object getValue(String paramName) {
		String patchedParamName = paramName;
		Object value = null;

		try {
			patchedParamName = this.patchParamName(paramName);
			value = super.getValue(patchedParamName);
		} catch (IllegalArgumentException e) {
			if (!this.isFallback(patchedParamName)) {
				throw e;
			}
		}

		if (value == null && this.isFallback(patchedParamName)) {
			value = this.fallback(patchedParamName);
		}

		value = this.converter.convert(patchedParamName, value);
		if (this.paddingIterableParams) {
			value = IterableExpandPadding.expandIfIterable(
				value,
				padArray,
				this.paddingIterableBoundaries
			);
		}

		return value;
	}

	/**
	 * Sets padding iterable boundaries.
	 *
	 * @param paddingIterableBoundaries the padding iterable boundaries
	 */
	public void setPaddingIterableBoundaries(int @Nullable [] paddingIterableBoundaries) {
		this.paddingIterableBoundaries = paddingIterableBoundaries;
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

	private @Nullable Object fallback(String paramName) {
		Assert.notNull(fallbackParameterSource, "FallbackParameterSource must not be null to fallback.");

		return this.fallbackParameterSource.fallback(paramName);
	}

	private String patchParamName(String paramName) {
		if (!StringUtils.hasText(prefix)) {
			return paramName;
		}

		if (!paramName.startsWith(prefix)) {
			throw new IllegalArgumentException("Param name does not starts with " + this.prefix);
		}

		return paramName.substring(prefix.length());
	}
}
