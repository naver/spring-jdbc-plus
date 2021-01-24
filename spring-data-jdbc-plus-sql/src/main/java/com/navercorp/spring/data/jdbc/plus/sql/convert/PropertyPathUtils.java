package com.navercorp.spring.data.jdbc.plus.sql.convert;

import javax.annotation.Nullable;

import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.Assert;

import com.navercorp.spring.data.jdbc.plus.sql.annotation.SqlTableAlias;

public class PropertyPathUtils {
	static SqlIdentifier getColumnAlias(PersistentPropertyPathExtension path) {
		return getColumnAlias(path, path.getColumnName());
	}

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

	static SqlIdentifier getReverseColumnAlias(PersistentPropertyPathExtension path) {
		return getReverseColumnAlias(path, path.getReverseColumnName());
	}

	static SqlIdentifier getReverseColumnAlias(
		PersistentPropertyPathExtension path, SqlIdentifier reverseColumnName
	) {
		SqlIdentifier tableAlias = getTableAlias(path);
		return tableAlias == null ? reverseColumnName
			: reverseColumnName.transform(name -> tableAlias.getReference(IdentifierProcessing.NONE) + "_" + name);
	}

	// Refer from PersistentPropertyPathExtension#getTableAlias
	@Nullable
	public static SqlIdentifier getTableAlias(PersistentPropertyPathExtension path) {
		PersistentPropertyPathExtension tableOwner = getTableOwningAncestor(path);
		return getTableAliasFromTableOwner(tableOwner);
	}

	@Nullable
	private static SqlIdentifier getTableAliasFromTableOwner(PersistentPropertyPathExtension tableOwner) {
		if (tableOwner.getLength() > 0) {	// path != null
			return assembleTableAlias(tableOwner);
		}

		// path == null : root
		SqlTableAlias sqlTableAlias = tableOwner.getLeafEntity().findAnnotation(SqlTableAlias.class);
		if (sqlTableAlias != null) {
			return SqlIdentifier.quoted(sqlTableAlias.value());
		}

		return null;
	}

	private static PersistentPropertyPathExtension getTableOwningAncestor(PersistentPropertyPathExtension path) {
		return path.isEntity() && !path.isEmbedded() ? path : getTableOwningAncestor(path.getParentPath());
	}

	private static SqlIdentifier assembleTableAlias(PersistentPropertyPathExtension path) {

		Assert.state(path != null, "Path is null");

		PersistentPropertyPath<? extends RelationalPersistentProperty> propertyPath = path.getRequiredPersistentPropertyPath();
		RelationalPersistentProperty leafProperty = propertyPath.getRequiredLeafProperty();

		String prefix;
		if (path.isEmbedded()) {
			prefix = leafProperty.getEmbeddedPrefix();
		} else {
			SqlTableAlias sqlTableAlias = leafProperty.findPropertyOrOwnerAnnotation(SqlTableAlias.class);
			prefix = sqlTableAlias != null
				? sqlTableAlias.value()
				: leafProperty.getName();
		}

		if (path.getLength() == 1) {
			Assert.notNull(prefix, "Prefix mus not be null.");
			return SqlIdentifier.quoted(prefix);
		}

		PersistentPropertyPathExtension parentPath = path.getParentPath();
		SqlIdentifier sqlIdentifier = assembleTableAlias(parentPath);

		return parentPath.isEmbedded() ? sqlIdentifier.transform(name -> name.concat(prefix))
			: sqlIdentifier.transform(name -> name + "_" + prefix);
	}
}
