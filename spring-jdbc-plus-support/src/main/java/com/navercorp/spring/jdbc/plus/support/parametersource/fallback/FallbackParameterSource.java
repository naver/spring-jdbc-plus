package com.navercorp.spring.jdbc.plus.support.parametersource.fallback;

public interface FallbackParameterSource {
	boolean isFallback(String paramName);

	Object fallback(String paramName);
}
