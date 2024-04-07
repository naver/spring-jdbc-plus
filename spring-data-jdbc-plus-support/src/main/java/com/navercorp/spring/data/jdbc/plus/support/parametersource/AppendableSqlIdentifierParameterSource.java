package com.navercorp.spring.data.jdbc.plus.support.parametersource;

import java.util.Set;

import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public interface AppendableSqlIdentifierParameterSource extends SqlParameterSource {

	static AppendableSqlIdentifierParameterSource create() {
		return new SqlIdentifierParameterSource();
	}

	Set<SqlIdentifier> getIdentifiers();

	void addValue(SqlIdentifier name, Object value);

	void addValue(SqlIdentifier identifier, Object value, int sqlType);

	void addAll(AppendableSqlIdentifierParameterSource others);
}
