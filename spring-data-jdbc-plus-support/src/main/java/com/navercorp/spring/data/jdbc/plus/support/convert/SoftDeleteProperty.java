package com.navercorp.spring.data.jdbc.plus.support.convert;

import java.util.Arrays;

import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn;

/**
 * A property to generate sql for soft delete.
 *
 * @since 3.3
 */
class SoftDeleteProperty {
	private static final SoftDeleteProperty NOT_EXISTS = new SoftDeleteProperty(false, SqlIdentifier.EMPTY, "");

	private final boolean exists;

	private final SqlIdentifier columnName;

	private final Object updateValue;

	private SoftDeleteProperty(boolean exists, SqlIdentifier columnName, @Nullable Object updateValue) {
		this.exists = exists;
		this.columnName = columnName;
		this.updateValue = updateValue;
	}


	static SoftDeleteProperty from(RelationalPersistentEntity<?> entity) {
		RelationalPersistentProperty softDeleteProperty = findSoftDeleteProperty(entity);
		if (softDeleteProperty == null) {
			return NOT_EXISTS;
		}

		return new SoftDeleteProperty(
			true,
			softDeleteProperty.getColumnName(),
			getSoftDeleteColumnUpdateValue(softDeleteProperty)
		);
	}

	@Nullable
	private static RelationalPersistentProperty findSoftDeleteProperty(RelationalPersistentEntity<?> entity) {
		for (RelationalPersistentProperty property : entity) {
			if (property.isAnnotationPresent(SoftDeleteColumn.class)) {
				return property;
			}
		}

		return null;
	}

	private static Object getSoftDeleteColumnUpdateValue(RelationalPersistentProperty property) {
		SoftDeleteColumn annotation = property.getRequiredAnnotation(SoftDeleteColumn.class);
		if (!StringUtils.hasText(annotation.valueAsDeleted())) {
			throw new IllegalArgumentException("SoftDeleteColum.valueAsDeleted() is Empty.");
		}

		return switch (annotation.type()) {
			case BOOLEAN -> getBooleanUpdateValue(annotation.valueAsDeleted());
			case STRING -> getStringUpdateValue(property, annotation.valueAsDeleted());
		};
	}

	private static boolean getBooleanUpdateValue(String value) {
		if ("true".equals(value) || "false".equals(value)) {
			return java.lang.Boolean.parseBoolean(value);
		}

		throw new IllegalArgumentException(
			"Invalid value %s provided for Boolean type of SoftDeleteColumn".formatted(value)
		);
	}

	private static Object getStringUpdateValue(
		RelationalPersistentProperty property,
		String value
	) {
		Class<?> actualType = property.getActualType();

		// if actual type is not enum, just return string value.
		if (!actualType.isEnum()) {
			return value;
		}

		// if actual type is enum type, cast to actual enum value to use type converter.
		return Arrays.stream((Enum<?>[])actualType.getEnumConstants())
			.filter(enumValue -> enumValue.name().equals(value))
			.findFirst()
			.orElseThrow(() ->
				new IllegalArgumentException(
					"Invalid enum name is provided. type: %s, name: %s".formatted(
						actualType.getSimpleName(),
						value
					)
				)
			);
	}

	public boolean exists() {
		return exists;
	}

	public SqlIdentifier getColumnName() {
		requireExists();

		return columnName;
	}

	public Object getUpdateValue() {
		requireExists();

		return updateValue;
	}

	private void requireExists() {
		if (!exists) {
			throw new IllegalStateException("SoftDeleteProperty should exist to use columnName/updateValue.");
		}
	}
}
