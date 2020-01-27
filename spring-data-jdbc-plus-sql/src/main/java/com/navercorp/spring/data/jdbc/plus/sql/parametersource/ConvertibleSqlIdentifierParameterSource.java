package com.navercorp.spring.data.jdbc.plus.sql.parametersource;

import java.util.Objects;

import org.springframework.data.relational.core.sql.IdentifierProcessing;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.IterableExpandPadding;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

class ConvertibleSqlIdentifierParameterSource extends SqlIdentifierParameterSource {
	private final JdbcParameterSourceConverter converter;
	private final FallbackParameterSource fallbackParameterSource;

	private boolean paddingIterableParams = false;
	private int[] paddingIterableBoundaries = null;

	ConvertibleSqlIdentifierParameterSource(
		IdentifierProcessing identifierProcessing,
		JdbcParameterSourceConverter converter,
		FallbackParameterSource fallbackParameterSource) {

		super(identifierProcessing);
		this.converter = Objects.requireNonNull(converter, "Converter must not be null.");
		this.fallbackParameterSource = fallbackParameterSource;
	}

	@Override
	public Object getValue(String paramName) throws IllegalArgumentException {
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

	public void setPaddingIterableBoundaries(int[] setPaddingIterableBoundaries) {
		this.paddingIterableBoundaries = setPaddingIterableBoundaries;
	}

	public void setPaddingIterableParam(boolean padding) {
		this.paddingIterableParams = padding;
	}

	private boolean isFallback(String paramName) {
		return this.fallbackParameterSource != null && this.fallbackParameterSource.isFallback(paramName);
	}
}
