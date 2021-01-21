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

import javax.annotation.Nullable;

import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.util.Assert;

import com.navercorp.spring.data.jdbc.plus.sql.annotation.SqlTableAlias;

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
		this.table = Table.create(entity.getTableName());
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
		SqlIdentifier tableAlias = this.getTableAlias(path);
		Table table = Table.create(path.getTableName());
		return tableAlias == null ? table : table.as(tableAlias);
	}

	@Override
	public Column getColumn(PersistentPropertyPathExtension path) {
		return getTable(path).column(path.getColumnName()).as(path.getColumnAlias());
	}

	@Override
	public Column getReverseColumn(PersistentPropertyPathExtension path) {
		return getTable(path).column(path.getReverseColumnName()).as(path.getReverseColumnNameAlias());
	}

	// Refer from PersistentPropertyPathExtension#getTableAlias
	@Nullable
	private SqlIdentifier getTableAlias(PersistentPropertyPathExtension path) {
		PersistentPropertyPathExtension tableOwner = getTableOwningAncestor(path);
		if (tableOwner.getLength() > 0) {	// path != null
			return this.assembleTableAlias(tableOwner);
		}

		// path == null : root
		SqlTableAlias sqlTableAlias = tableOwner.getLeafEntity().findAnnotation(SqlTableAlias.class);
		if (sqlTableAlias != null) {
			return this.table.as(sqlTableAlias.value()).getReferenceName();
		}

		return null;
	}

	private PersistentPropertyPathExtension getTableOwningAncestor(PersistentPropertyPathExtension path) {
		return path.isEntity() && !path.isEmbedded() ? path : this.getTableOwningAncestor(path.getParentPath());
	}

	private SqlIdentifier assembleTableAlias(PersistentPropertyPathExtension path) {

		Assert.state(path != null, "Path is null");

		PersistentPropertyPath<? extends RelationalPersistentProperty> propertyPath = path.getRequiredPersistentPropertyPath();
		RelationalPersistentProperty leafProperty = propertyPath.getRequiredLeafProperty();

		String prefix;
		if (path.isEmbedded()) {
			prefix = leafProperty.getEmbeddedPrefix();
		} else {
			SqlTableAlias sqlTableAlias = leafProperty.findPropertyOrOwnerAnnotation(SqlTableAlias.class);
			prefix = sqlTableAlias != null ? sqlTableAlias.value() : leafProperty.getName();
		}

		if (path.getLength() == 1) {
			Assert.notNull(prefix, "Prefix mus not be null.");
			return SqlIdentifier.quoted(prefix);
		}

		PersistentPropertyPathExtension parentPath = path.getParentPath();
		SqlIdentifier sqlIdentifier = this.assembleTableAlias(parentPath);

		return parentPath.isEmbedded() ? sqlIdentifier.transform(name -> name.concat(prefix))
			: sqlIdentifier.transform(name -> name + "_" + prefix);
	}
}
