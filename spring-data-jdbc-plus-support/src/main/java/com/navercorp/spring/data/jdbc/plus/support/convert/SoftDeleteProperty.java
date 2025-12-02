package com.navercorp.spring.data.jdbc.plus.support.convert;

import org.jspecify.annotations.Nullable;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.Assert;

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
	static SoftDeleteProperty from(@Nullable RelationalPersistentEntity<?> entity) {
		Assert.state(entity != null, "RelationalPersistentEntity must not be null");

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
