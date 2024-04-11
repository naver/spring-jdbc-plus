package com.navercorp.spring.data.jdbc.plus.support.convert;

import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.SqlIdentifier;


/**
 * A property to generate sql for soft delete.
 *
 * @since 3.3
 */
public interface SoftDeleteProperty {


	static SoftDeleteProperty from(RelationalPersistentEntity<?> entity) {
		return DefaultSoftDeleteProperty.from(entity);
	}


	boolean exists();

	SqlIdentifier getColumnName();

	Object getUpdateValue();
}
