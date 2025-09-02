/*
 * Copyright 2020-2025 the original author or authors.
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

package com.navercorp.spring.data.jdbc.plus.support.convert;

import javax.annotation.Nullable;

import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.AggregatePathTraversal;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
	public static SqlIdentifier getColumnAlias(AggregatePath path) {
		return getColumnAlias(path, path.getColumnInfo().name());
	}

	/**
	 * getColumnAlias applied @SqlTableAlias
	 *
	 * @param path
	 * @param columnName
	 * @return
	 */
	public static SqlIdentifier getColumnAlias(AggregatePath path, SqlIdentifier columnName) {
		AggregatePath tableOwner = AggregatePathTraversal.getTableOwningPath(path);
		if (tableOwner.isRoot()) {
			return columnName;
		} else {
			SqlIdentifier tableAlias = getTableAliasFromTableOwner(tableOwner);
			return tableAlias == null ? columnName
				: columnName.transform(name -> tableAlias.getReference() + "_" + name);
		}
	}

	/**
	 * getReverseColumnAlias applied @SqlTableAlias
	 *
	 * @param path
	 * @return
	 */
	public static SqlIdentifier getReverseColumnAlias(AggregatePath path) {
		return getReverseColumnAlias(path, path.getTableInfo().backReferenceColumnInfos().any().name());
	}

	/**
	 * getReverseColumnAlias applied @SqlTableAlias
	 *
	 * @param path
	 * @param reverseColumnName
	 * @return
	 */
	public static SqlIdentifier getReverseColumnAlias(
		AggregatePath path,
		SqlIdentifier reverseColumnName
	) {
		SqlIdentifier tableAlias = getTableAlias(path);
		return tableAlias == null
			? reverseColumnName
			: reverseColumnName.transform(name -> tableAlias.getReference() + "_" + name);
	}

	/**
	 * getTableAlias applied @SqlTableAlias
	 * Refer from AggregatePath#getTableInfo#tableAlias
	 *
	 * @param path
	 * @return
	 */
	@Nullable
	public static SqlIdentifier getTableAlias(AggregatePath path) {
		AggregatePath tableOwner = AggregatePathTraversal.getTableOwningPath(path);
		return getTableAliasFromTableOwner(tableOwner);
	}

	@Nullable
	private static SqlIdentifier getTableAliasFromTableOwner(AggregatePath tableOwner) {
		if (!tableOwner.isRoot()) {    // path != null
			return assembleTableAlias(tableOwner);
		}

		// path == null : root
		return TableAliasUtils.getTableAlias(tableOwner.getLeafEntity());
	}

	@Nullable
	private static SqlIdentifier assembleTableAlias(AggregatePath path) {

		Assert.state(path != null, "Path is null");
		if (path.isRoot()) {
			return null;
		}

		String prefix = TableAliasUtils.getTableAliasPropertyPathPrefix(path);
		Assert.notNull(prefix, "Prefix must not be null.");

		if (path.getParentPath().isRoot()) {
			return StringUtils.hasText(prefix) ? SqlIdentifier.quoted(prefix) : null;
		}

		AggregatePath parentPath = path.getParentPath();
		SqlIdentifier sqlIdentifier = assembleTableAlias(parentPath);
		if (sqlIdentifier != null) {
			return parentPath.isEmbedded() ? sqlIdentifier.transform(name -> name.concat(prefix))
				: sqlIdentifier.transform(name -> name + "_" + prefix);
		}

		return SqlIdentifier.quoted(prefix);
	}
}
