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

package com.navercorp.spring.jdbc.plus.support.parametersource;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.lang.Nullable;
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
	private final FallbackParameterSource fallbackParameterSource;
	private final String prefix;

	private boolean paddingIterableParams = false;
	private int[] paddingIterableBoundaries = null;

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
		FallbackParameterSource fallbackParameterSource) {

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
		String prefix,
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
		String prefix,
		Object bean,
		JdbcParameterSourceConverter converter,
		FallbackParameterSource fallbackParameterSource
	) {

		super(bean);
		this.prefix = prefix != null ? prefix.trim() : null;
		this.converter = Objects.requireNonNull(converter, "Converter must not be null.");
		this.fallbackParameterSource = fallbackParameterSource;
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
			value = this.fallbackParameterSource.fallback(patchedParamName);
		}

		value = this.converter.convert(patchedParamName, value);
		if (this.paddingIterableParams) {
			value = IterableExpandPadding.expandIfIterable(value, this.paddingIterableBoundaries);
		}

		return value;
	}

	/**
	 * Sets padding iterable boundaries.
	 *
	 * @param paddingIterableBoundaries the padding iterable boundaries
	 */
	public void setPaddingIterableBoundaries(int[] paddingIterableBoundaries) {
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
