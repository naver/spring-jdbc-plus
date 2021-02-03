/*
 * Copyright 2020-2021 the original author or authors.
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

import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.Assert;

/**
 * PropertyPathUtils to get ColumnAlias and TableAlias applied @SqlTableAlias
 * This methods call is for internal interlocking purposes. Do not call directly.
 *
 * @author Myeonghyeon Lee
 */
public class PropertyPathUtils {
	/**
	 * getColumnAlias applied @SqlTableAlias
	 *
	 * @param path
	 * @return
	 */
	static SqlIdentifier getColumnAlias(PersistentPropertyPathExtension path) {
		return getColumnAlias(path, path.getColumnName());
	}

	/**
	 * getColumnAlias applied @SqlTableAlias
	 *
	 * @param path
	 * @param columnName
	 * @return
	 */
	static SqlIdentifier getColumnAlias(PersistentPropertyPathExtension path, SqlIdentifier columnName) {
		PersistentPropertyPathExtension tableOwner = getTableOwningAncestor(path);
		if (tableOwner.getLength() == 0) {
			return columnName;
		} else {
			SqlIdentifier tableAlias = getTableAliasFromTableOwner(tableOwner);
			return tableAlias == null ? columnName
				: columnName.transform(name -> tableAlias.getReference(IdentifierProcessing.NONE) + "_" + name);
		}
	}

	/**
	 * getReverseColumnAlias applied @SqlTableAlias
	 *
	 * @param path
	 * @return
	 */
	static SqlIdentifier getReverseColumnAlias(PersistentPropertyPathExtension path) {
		return getReverseColumnAlias(path, path.getReverseColumnName());
	}

	/**
	 * getReverseColumnAlias applied @SqlTableAlias
	 *
	 * @param path
	 * @param reverseColumnName
	 * @return
	 */
	static SqlIdentifier getReverseColumnAlias(
		PersistentPropertyPathExtension path, SqlIdentifier reverseColumnName
	) {
		SqlIdentifier tableAlias = getTableAlias(path);
		return tableAlias == null ? reverseColumnName
			: reverseColumnName.transform(name -> tableAlias.getReference(IdentifierProcessing.NONE) + "_" + name);
	}

	/**
	 * getTableAlias applied @SqlTableAlias
	 * Refer from PersistentPropertyPathExtension#getTableAlias
	 *
	 * @param path
	 * @return
	 */
	@Nullable
	static SqlIdentifier getTableAlias(PersistentPropertyPathExtension path) {
		PersistentPropertyPathExtension tableOwner = getTableOwningAncestor(path);
		return getTableAliasFromTableOwner(tableOwner);
	}

	@Nullable
	private static SqlIdentifier getTableAliasFromTableOwner(PersistentPropertyPathExtension tableOwner) {
		if (tableOwner.getLength() > 0) {	// path != null
			return assembleTableAlias(tableOwner);
		}

		// path == null : root
		return TableAliasUtils.getTableAlias(tableOwner.getLeafEntity());
	}

	private static PersistentPropertyPathExtension getTableOwningAncestor(PersistentPropertyPathExtension path) {
		return path.isEntity() && !path.isEmbedded() ? path : getTableOwningAncestor(path.getParentPath());
	}

	private static SqlIdentifier assembleTableAlias(PersistentPropertyPathExtension path) {

		Assert.state(path != null, "Path is null");

		String prefix = TableAliasUtils.getTableAliasPropertyPathPrefix(path);
		if (path.getLength() == 1) {
			Assert.notNull(prefix, "Prefix must not be null.");
			return SqlIdentifier.quoted(prefix);
		}

		PersistentPropertyPathExtension parentPath = path.getParentPath();
		SqlIdentifier sqlIdentifier = assembleTableAlias(parentPath);

		return parentPath.isEmbedded() ? sqlIdentifier.transform(name -> name.concat(prefix))
			: sqlIdentifier.transform(name -> name + "_" + prefix);
	}
}
