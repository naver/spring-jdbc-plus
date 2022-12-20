/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.relational.core.sql.Table;

/**
 * Utility to get from path to SQL DSL elements.
 *
 * @author Jens Schauder
 * @author Mark Paluch
 * @author Tyler Van Gorder
 * @author Myeonghyeon Lee
 * @since 1.1

 * Copy org.springframework.data.jdbc.core.convert.SqlContext
 * Verified: 592f483699fda8ef5d6ba846162dffd54a922c94
 */
class SqlContext implements SqlContexts {

	private final RelationalPersistentEntity<?> entity;
	private final Table table;

	SqlContext(RelationalPersistentEntity<?> entity) {
		this.entity = entity;
		this.table = getEntityTable(entity);
	}

	private static Table getEntityTable(RelationalPersistentEntity<?> entity) {
		Table table = Table.create(entity.getQualifiedTableName());
		SqlIdentifier tableAlias = TableAliasUtils.getTableAlias(entity);
		if (tableAlias != null) {
			table = table.as(tableAlias);
		}
		return table;
	}

	@Override
	public Column getIdColumn() {
		return table.column(entity.getIdColumn());
	}

	@Override
	public Column getVersionColumn() {
		return table.column(entity.getRequiredVersionProperty().getColumnName());
	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	public Table getTable(PersistentPropertyPathExtension path) {
		SqlIdentifier tableAlias = PropertyPathUtils.getTableAlias(path);
		Table table = Table.create(path.getQualifiedTableName());
		return tableAlias == null ? table : table.as(tableAlias);
	}

	@Override
	public Column getColumn(PersistentPropertyPathExtension path) {
		SqlIdentifier columnName = path.getColumnName();
		SqlIdentifier columnAlias = PropertyPathUtils.getColumnAlias(path, columnName);
		return getTable(path).column(columnName).as(columnAlias);
	}

	@Override
	public Column getReverseColumn(PersistentPropertyPathExtension path) {
		SqlIdentifier reverseColumnName = path.getReverseColumnName();
		SqlIdentifier reverseColumnAlias = PropertyPathUtils.getReverseColumnAlias(path, reverseColumnName);
		return getTable(path).column(reverseColumnName).as(reverseColumnAlias);
	}
}
