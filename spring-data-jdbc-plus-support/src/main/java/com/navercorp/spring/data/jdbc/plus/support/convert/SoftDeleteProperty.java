package com.navercorp.spring.data.jdbc.plus.support.convert;

import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.SqlIdentifier;

/**
 * A property to generate sql for soft delete.
 *
 * @since 3.3
 */
public interface SoftDeleteProperty {

	/**
	 * Create a new property from {@link RelationalPersistentEntity}.
	 *
	 * @param entity
	 * @return
	 */
	static SoftDeleteProperty from(RelationalPersistentEntity<?> entity) {
		return DefaultSoftDeleteProperty.from(entity);
	}

	/**
	 * Returns whether this property supports soft delete or not.
	 *
	 * @return true when supports soft delete.
	 */
	boolean exists();

	/**
	 * Returns the column name used for soft delete.
	 *
	 * @return the column name including the schema.
	 */
	SqlIdentifier getColumnName();

	/**
	 * Returns the value used for executing soft delete.
	 *
	 * @return the value which is considered as deleted.
	 */
	Object getUpdateValue();
}
