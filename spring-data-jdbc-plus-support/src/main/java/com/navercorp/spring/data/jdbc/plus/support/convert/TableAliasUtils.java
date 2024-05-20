package com.navercorp.spring.data.jdbc.plus.support.convert;

import javax.annotation.Nullable;

import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;

import com.navercorp.spring.jdbc.plus.commons.annotations.SqlTableAlias;

/**
 * TableAliasUtils to get table alias applied @SqlTableAlias
 * This methods call is for internal interlocking purposes. Do not call directly.
 *
 * @author Myeonghyeon Lee
 */
class TableAliasUtils {
	/**
	 * getTableAlias applied @SqlTableAlias
	 *
	 * @param entity
	 * @return
	 */
	@Nullable
	static SqlIdentifier getTableAlias(RelationalPersistentEntity<?> entity) {
		SqlTableAlias sqlTableAlias = entity.findAnnotation(SqlTableAlias.class);
		if (sqlTableAlias != null) {
			return SqlIdentifier.quoted(sqlTableAlias.value());
		}

		return null;
	}

	/**
	 * getTableAliasPropertyPathPrefix property path prefix applied @SqlTableAlias
	 *
	 * @param path
	 * @return
	 */
	@Nullable
	static String getTableAliasPropertyPathPrefix(AggregatePath path) {
		if (path.isRoot()) {
			return null;
		}

		PersistentPropertyPath<RelationalPersistentProperty> propertyPath =
			path.getRequiredPersistentPropertyPath();
		RelationalPersistentProperty leafProperty = propertyPath.getLeafProperty();

		if (path.isEmbedded()) {
			return leafProperty.getEmbeddedPrefix();
		}

		SqlTableAlias sqlTableAlias = leafProperty.findAnnotation(SqlTableAlias.class);

		return sqlTableAlias != null ? sqlTableAlias.value() : leafProperty.getName();
	}
}
