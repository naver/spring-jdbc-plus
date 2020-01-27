package com.navercorp.spring.jdbc.plus.support.parametersource.fallback;

public class NoneFallbackParameterSource implements FallbackParameterSource {
	@Override
	public boolean isFallback(String paramName) {
		return false;
	}

	@Override
	public Object fallback(String paramName) {
		return null;
	}
}
