package com.navercorp.spring.data.jdbc.plus.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SqlFunction {
	String COLUMN_NAME = "${columnName}";

	@AliasFor("functionName")
	String value() default "COALESCE";

	@AliasFor("value")
	String functionName() default "COALESCE";

	String[] expressions() default {COLUMN_NAME};
}
