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

import java.util.Map;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

/**
 * The type Convertible parameter source factory.
 *
 * @author Myeonghyeon Lee
 * @author IAM20
 */
public class ConvertibleParameterSourceFactory {
	private final JdbcParameterSourceConverter converter;
	private final FallbackParameterSource fallbackParameterSource;

	private boolean padArray = true;
	private boolean paddingIterableParams = false;
	private int[] paddingIterableBoundaries = null;

	/**
	 * Instantiates a new Convertible parameter source factory.
	 */
	public ConvertibleParameterSourceFactory() {
		this((p, v) -> v, null);
	}

	/**
	 * Instantiates a new Convertible parameter source factory.
	 *
	 * @param converter               the converter
	 * @param fallbackParameterSource the fallback parameter source
	 */
	public ConvertibleParameterSourceFactory(
		JdbcParameterSourceConverter converter, FallbackParameterSource fallbackParameterSource) {

		this.converter = converter;
		this.fallbackParameterSource = fallbackParameterSource;
	}

	/**
	 * Bean parameter source bean property sql parameter source.
	 *
	 * @param bean the bean
	 * @return the bean property sql parameter source
	 */
	public BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		ConvertibleBeanPropertySqlParameterSource paramSource =
			new ConvertibleBeanPropertySqlParameterSource(
				bean, this.converter, this.fallbackParameterSource);
		paramSource.setPaddingIterableParam(this.paddingIterableParams);
		paramSource.setPaddingIterableBoundaries(this.paddingIterableBoundaries);
		return paramSource;
	}

	/**
	 * Bean parameter source bean property sql parameter source.
	 *
	 * @param prefix the prefix
	 * @param bean   the bean
	 * @return the bean property sql parameter source
	 */
	public BeanPropertySqlParameterSource beanParameterSource(String prefix, Object bean) {
		ConvertibleBeanPropertySqlParameterSource paramSource =
			new ConvertibleBeanPropertySqlParameterSource(
				prefix, bean, this.converter, this.fallbackParameterSource);
		paramSource.setPaddingIterableParam(this.paddingIterableParams);
		paramSource.setPaddingIterableBoundaries(this.paddingIterableBoundaries);
		return paramSource;
	}

	/**
	 * Map parameter source map sql parameter source.
	 *
	 * @param map the map
	 * @return the map sql parameter source
	 */
	public MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		ConvertibleMapSqlParameterSource paramSource =
			new ConvertibleMapSqlParameterSource(map, this.converter, this.fallbackParameterSource);
		paramSource.setPaddingIterableParam(this.paddingIterableParams);
		paramSource.setPaddingIterableBoundaries(this.paddingIterableBoundaries);
		paramSource.setPadArray(this.padArray);
		return paramSource;
	}

	/**
	 * Sets padding iterable param.
	 *
	 * @param padding the padding
	 */
	public void setPaddingIterableParam(boolean padding) {
		this.paddingIterableParams = padding;
	}

	/**
	 * Get padding iterable boundaries int [ ].
	 *
	 * @return the int [ ]
	 */
	public int[] getPaddingIterableBoundaries() {
		return this.paddingIterableBoundaries;
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
	 * Is padding iterable params boolean.
	 *
	 * @return the boolean
	 */
	public boolean isPaddingIterableParams() {
		return this.paddingIterableParams;
	}

	/**
	 * Gets converter.
	 *
	 * @return the converter
	 */
	public JdbcParameterSourceConverter getConverter() {
		return this.converter;
	}

	/**
	 * Gets fallback.
	 *
	 * @return the fallback
	 */
	public FallbackParameterSource getFallback() {
		return this.fallbackParameterSource;
	}
}
