package com.navercorp.spring.jdbc.plus.support.parametersource;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.navercorp.spring.jdbc.plus.support.parametersource.converter.JdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.FallbackParameterSource;

public class ConvertibleParameterSourceFactory {
	private final JdbcParameterSourceConverter converter;
	private final FallbackParameterSource fallbackParameterSource;

	private boolean paddingIterableParams = false;
	private int[] paddingIterableBoundaries = null;

	public ConvertibleParameterSourceFactory() {
		this((p, v) -> v, null);
	}

	public ConvertibleParameterSourceFactory(JdbcParameterSourceConverter converter, FallbackParameterSource fallbackParameterSource) {
		this.converter = converter;
		this.fallbackParameterSource = fallbackParameterSource;
	}

	public BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		ConvertibleBeanPropertySqlParameterSource paramSource =
			new ConvertibleBeanPropertySqlParameterSource(bean, this.converter, this.fallbackParameterSource);
		paramSource.setPaddingIterableParam(this.paddingIterableParams);
		paramSource.setPaddingIterableBoundaries(this.paddingIterableBoundaries);
		return paramSource;
	}

	public MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		ConvertibleMapSqlParameterSource paramSource =
			new ConvertibleMapSqlParameterSource(map, this.converter, this.fallbackParameterSource);
		paramSource.setPaddingIterableParam(this.paddingIterableParams);
		paramSource.setPaddingIterableBoundaries(this.paddingIterableBoundaries);
		return paramSource;
	}

	public void setPaddingIterableParam(boolean padding) {
		this.paddingIterableParams = padding;
	}

	public int[] getPaddingIterableBoundaries() {
		return this.paddingIterableBoundaries;
	}

	public void setPaddingIterableBoundaries(int[] paddingIterableBoundaries) {
		this.paddingIterableBoundaries = paddingIterableBoundaries;
	}

	public boolean isPaddingIterableParams() {
		return this.paddingIterableParams;
	}

	public JdbcParameterSourceConverter getConverter() {
		return this.converter;
	}

	public FallbackParameterSource getFallback() {
		return this.fallbackParameterSource;
	}
}
