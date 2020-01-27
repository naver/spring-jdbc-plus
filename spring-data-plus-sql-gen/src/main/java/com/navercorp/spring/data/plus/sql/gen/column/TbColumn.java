package com.navercorp.spring.data.plus.sql.gen.column;

import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

public final class TbColumn {
	private final PersistentPropertyPathExtension pathExtension;
	private final IdentifierProcessing identifierProcessing;

	private final String path;
	private final String column;
	private final String alias;

	TbColumn(PersistentPropertyPathExtension pathExtension, IdentifierProcessing identifierProcessing) {
		this.pathExtension = pathExtension;
		this.identifierProcessing = identifierProcessing;

		this.path = pathExtension.getRequiredPersistentPropertyPath().toDotPath();
		this.column = pathExtension.getColumnName().getReference(identifierProcessing);
		String aliasValue = this.column;
		if (pathExtension.getColumnAlias() != null) {
			aliasValue = pathExtension.getColumnAlias().getReference(identifierProcessing);
		}
		this.alias = aliasValue;
	}

	public static TbColumn create(PersistentPropertyPathExtension pathExtension, IdentifierProcessing identifierProcessing) {
		return new TbColumn(pathExtension, identifierProcessing);
	}

	public String path() {
		return this.path;
	}

	public String column() {
		return this.column;
	}

	public String alias() {
		return this.alias;
	}
}
