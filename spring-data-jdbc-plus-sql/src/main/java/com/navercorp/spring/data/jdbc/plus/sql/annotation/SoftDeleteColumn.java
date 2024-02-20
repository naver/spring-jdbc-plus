package com.navercorp.spring.data.jdbc.plus.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
public @interface SoftDeleteColumn {
	DeleteValueType deleteValueType();
	String deleteValueString() default "";
	boolean deleteValueBoolean() default false;

	enum DeleteValueType {
		USE_CODE, USE_BOOLEAN
	}

	@SoftDeleteColumn(deleteValueType = DeleteValueType.USE_CODE)
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Code {
		@AliasFor(annotation = SoftDeleteColumn.class, attribute = "deleteValueString")
		String deleteValue();
	}

	@SoftDeleteColumn(deleteValueType = DeleteValueType.USE_BOOLEAN)
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Boolean {
		@AliasFor(annotation = SoftDeleteColumn.class, attribute = "deleteValueBoolean")
		boolean deleteValue() default false;
	}
}
