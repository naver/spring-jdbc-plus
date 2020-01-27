package com.navercorp.spring.data.jdbc.plus.sql.provider;

public class IllegalReturnTypeException extends IllegalArgumentException {
	public IllegalReturnTypeException(String message, Exception ex) {
		super(message, ex);
	}
}
