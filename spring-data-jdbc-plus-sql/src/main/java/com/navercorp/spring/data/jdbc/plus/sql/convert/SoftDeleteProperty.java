package com.navercorp.spring.data.jdbc.plus.sql.convert;

import java.util.Arrays;

import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.lang.Nullable;

import com.navercorp.spring.data.jdbc.plus.sql.annotation.SoftDeleteColumn;

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
		SoftDeleteColumn.Boolean booleanAnnotation = property.findAnnotation(SoftDeleteColumn.Boolean.class);
		SoftDeleteColumn.String stringAnnotation = property.findAnnotation(SoftDeleteColumn.String.class);

		if (booleanAnnotation != null) {
			return booleanAnnotation.valueAsDeleted();
		}

		if (stringAnnotation != null) {
			return getUpdateValueFromString(property, stringAnnotation.valueAsDeleted());
		}

		throw new IllegalArgumentException("SoftDeleteColumn annotation not exists.");
	}

	private static Object getUpdateValueFromString(
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
