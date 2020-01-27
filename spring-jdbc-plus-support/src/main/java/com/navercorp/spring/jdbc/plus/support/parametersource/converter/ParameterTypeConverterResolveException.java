package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import org.springframework.core.convert.ConversionException;

public class ParameterTypeConverterResolveException extends ConversionException {
	public ParameterTypeConverterResolveException(String msg) {
		super(msg);
	}
}
