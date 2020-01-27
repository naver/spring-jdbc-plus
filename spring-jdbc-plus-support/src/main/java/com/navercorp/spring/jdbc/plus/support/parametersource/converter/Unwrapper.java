package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import org.springframework.lang.Nullable;

// check only unwrapping container type (ignore container generics)
public interface Unwrapper<T> {
	@Nullable
	Object unwrap(T value);
}
