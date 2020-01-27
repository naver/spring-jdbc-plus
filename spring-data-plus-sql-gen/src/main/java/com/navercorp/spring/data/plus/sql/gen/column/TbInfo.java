package com.navercorp.spring.data.plus.sql.gen.column;

import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

public final class TbInfo {
	private final PersistentPropertyPathExtension pathExtension;
	private final IdentifierProcessing identifierProcessing;

	private final String path;
	private final String table;
	private final String alias;

	TbInfo(PersistentPropertyPathExtension pathExtension, IdentifierProcessing identifierProcessing) {
		this.pathExtension = pathExtension;
		this.identifierProcessing =  identifierProcessing;

		this.path = pathExtension.getRequiredPersistentPropertyPath().toDotPath();
		this.table = pathExtension.getTableName().getReference(identifierProcessing);
		String aliasValue = this.table;
		if (pathExtension.getTableAlias() != null) {
			aliasValue = pathExtension.getTableAlias().getReference(identifierProcessing);
		}
		this.alias = aliasValue;
	}

	public static TbInfo create(PersistentPropertyPathExtension pathExtension, IdentifierProcessing identifierProcessing) {
		return new TbInfo(pathExtension, identifierProcessing);
	}

	public String path() {
		return this.path;
	}

	public String table() {
		return this.table;
	}

	public String alias() {
		return this.alias;
	}
}
