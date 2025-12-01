package com.navercorp.spring.data.jdbc.plus.support.parametersource;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public interface MutableSqlIdentifierParameterSource extends SqlParameterSource {

	static MutableSqlIdentifierParameterSource create() {
		return new SqlIdentifierParameterSource();
	}

	Set<SqlIdentifier> getIdentifiers();

	void addValue(SqlIdentifier name, Object value);

	void addValue(SqlIdentifier identifier, @Nullable Object value, int sqlType);

	void addAll(MutableSqlIdentifierParameterSource others);
}
