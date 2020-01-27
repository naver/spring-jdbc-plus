package com.navercorp.spring.jdbc.plus.support.parametersource;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.lang.Nullable;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.IterableExpandPadding;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

public class ConvertibleBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {
	private final JdbcParameterSourceConverter converter;
	private final FallbackParameterSource fallbackParameterSource;

	private boolean paddingIterableParams = false;
	private int[] paddingIterableBoundaries = null;

	public ConvertibleBeanPropertySqlParameterSource(Object bean, JdbcParameterSourceConverter converter) {
		this(bean, converter, null);
	}

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

	public void setPaddingIterableBoundaries(int[] paddingIterableBoundaries) {
		this.paddingIterableBoundaries = paddingIterableBoundaries;
	}

	public void setPaddingIterableParam(boolean padding) {
		this.paddingIterableParams = padding;
	}

	private boolean isFallback(String paramName) {
		return this.fallbackParameterSource != null && this.fallbackParameterSource.isFallback(paramName);
	}
}
