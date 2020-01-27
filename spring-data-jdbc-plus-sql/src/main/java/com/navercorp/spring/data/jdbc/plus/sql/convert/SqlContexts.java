package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Table;

/**
 * Interface for internal extension.
 */
public interface SqlContexts {
	Column getIdColumn();

	Table getTable();

	Table getTable(PersistentPropertyPathExtension path);

	Column getColumn(PersistentPropertyPathExtension path);

	Column getVersionColumn();

	Column getReverseColumn(PersistentPropertyPathExtension path);
}
