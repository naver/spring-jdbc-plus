package com.navercorp.spring.jdbc.plus.commons.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * The annotation to configure a soft delete column in the current table.
 * <p>
 * There are two types available which are described in {@link ValueType}
 * </p>
 *
 * @since 3.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Documented
public @interface SoftDeleteColumn {

	ValueType type();

	java.lang.String valueAsDeleted() default "";

	/**
	 * Type fo value to be used for updating soft delete column.
	 *
	 * @since 3.3
	 */
	enum ValueType {
		BOOLEAN,

		/**
		 * It can be raw {@link String} or name of {@link Enum}.
		 */
		STRING
	}

	/**
	 * Shortcut for a boolean type of soft delete column
	 *
	 * <pre>
	 * <code>
	 * &#64;SoftDeleteColumn.Boolean(valueAsDeleted = true)
	 * private boolean deleted;
	 * </code>
	 * </pre>
	 *
	 * as alternative to the more verbose
	 *
	 * <pre>
	 * <code>
	 * &#64;SoftDeleteColumn.Boolean(valueAsDeleted = false)
	 * private boolean available;
	 * </code>
	 * </pre>
	 *
	 * @since 3.3
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD })
	@SoftDeleteColumn(type = ValueType.BOOLEAN)
	@interface Boolean {
		@AliasFor(annotation = SoftDeleteColumn.class, attribute = "valueAsDeleted")
		java.lang.String valueAsDeleted();
	}

	/**
	 * Shortcut for a string type of soft delete column.
	 *
	 * <pre>
	 * public enum State {
	 *     OPEN, CLOSE, DELETE
	 * }
	 * </pre>
	 *
	 * <pre>
	 * <code>
	 * &#64;SoftDeleteColumn.String(valueAsDeleted = "DELETE")
	 * private State state;
	 * </code>
	 * </pre>
	 *
	 * @since 3.3
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD })
	@SoftDeleteColumn(type = ValueType.STRING)
	@interface String {
		@AliasFor(annotation = SoftDeleteColumn.class, attribute = "valueAsDeleted")
		java.lang.String valueAsDeleted();
	}
}
