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

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.IterableExpandPadding;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

/**
 * The type Convertible bean property sql parameter source.
 *
 * @author Myeonghyeon Lee
 */
public class ConvertibleBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {
	private final JdbcParameterSourceConverter converter;
	private final FallbackParameterSource fallbackParameterSource;

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

		super(bean);
		this.converter = Objects.requireNonNull(converter, "Converter must not be null.");
		this.fallbackParameterSource = fallbackParameterSource;
	}

	@Nullable
	@Override
	public Object getValue(String paramName) {
		Object value = null;
		try {
			value = super.getValue(paramName);
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
}
