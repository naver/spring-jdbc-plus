package com.navercorp.spring.data.jdbc.plus.sql.parametersource;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public interface SqlParameterSourceFactory {
	BeanPropertySqlParameterSource beanParameterSource(Object bean);

	MapSqlParameterSource mapParameterSource(Map<String, ?> map);

	SqlParameterSource entityParameterSource(Object entity);
}
