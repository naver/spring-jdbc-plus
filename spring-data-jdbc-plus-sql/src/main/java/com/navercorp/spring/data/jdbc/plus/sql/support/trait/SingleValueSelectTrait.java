package com.navercorp.spring.data.jdbc.plus.sql.support.trait;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public interface SingleValueSelectTrait {
	NamedParameterJdbcOperations getJdbcOperations();

	default <T> T selectSingleValue(String sql, SqlParameterSource params, Class<T> returnType) {
		return this.getJdbcOperations().queryForObject(sql, params, returnType);
	}
}
