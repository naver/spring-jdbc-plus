package com.navercorp.spring.data.jdbc.plus.sql.convert;

import javax.annotation.Nullable;

import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;

import com.navercorp.spring.data.jdbc.plus.sql.annotation.SqlTableAlias;

/**
 * TableAliasUtils to get table alias applied @SqlTableAlias
 * This methods call is for internal interlocking purposes. Do not call directly.
 *
 * @author Myeonghyeon Lee
 */
public class TableAliasUtils {
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
	static String getTableAliasPropertyPathPrefix(PersistentPropertyPathExtension path) {
		PersistentPropertyPath<? extends RelationalPersistentProperty> propertyPath =
			path.getRequiredPersistentPropertyPath();
		RelationalPersistentProperty leafProperty = propertyPath.getRequiredLeafProperty();

		String prefix;
		if (path.isEmbedded()) {
			prefix = leafProperty.getEmbeddedPrefix();
		} else {
			SqlTableAlias sqlTableAlias = leafProperty.findAnnotation(SqlTableAlias.class);
			prefix = sqlTableAlias != null
				? sqlTableAlias.value()
				: leafProperty.getName();
		}

		return prefix;
	}
}
