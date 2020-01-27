package com.navercorp.spring.data.jdbc.plus.sql.provider;

import java.util.Map;

import com.navercorp.spring.jdbc.plus.support.parametersource.CompositeSqlParameterSource;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.mapping.MappingException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultSetExtractor;
import com.navercorp.spring.data.jdbc.plus.sql.convert.SqlProvider;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;

public class EntityJdbcProvider {
	private final NamedParameterJdbcOperations jdbcOperations;
	private final SqlProvider sqlProvider;
	private final SqlParameterSourceFactory sqlParameterSourceFactory;
	private final QueryMappingConfiguration queryMappingConfiguration;

	public EntityJdbcProvider(
		NamedParameterJdbcOperations jdbcOperations,
		SqlProvider sqlProvider,
		SqlParameterSourceFactory sqlParameterSourceFactory,
		QueryMappingConfiguration queryMappingConfiguration) {

		this.jdbcOperations = jdbcOperations;
		this.sqlProvider = sqlProvider;
		this.sqlParameterSourceFactory = sqlParameterSourceFactory;
		this.queryMappingConfiguration = queryMappingConfiguration;
	}

	public NamedParameterJdbcOperations getJdbcOperations() {
		return this.jdbcOperations;
	}

	public SqlProvider getSqlProvider() {
		return this.sqlProvider;
	}

	public SqlParameterSourceFactory getSqlParameterSourceFactory() {
		return this.sqlParameterSourceFactory;
	}

	public QueryMappingConfiguration getQueryMappingConfiguration() {
		return this.queryMappingConfiguration;
	}

	@SuppressWarnings("unchecked")
	public <T> RowMapper<T> getRowMapper(Class<T> entityType) {
		try {
			return (RowMapper<T>)this.queryMappingConfiguration.getRowMapper(entityType);
		} catch (MappingException e) {
			throw new IllegalReturnTypeException(
				"EntityRowMapper returnType must be entity type. returnType: " + entityType, e);
		}
	}

	public <T> AggregateResultSetExtractor<T> getAggregateResultSetExtractor(Class<T> entityType) {
		if (!EntityQueryMappingConfiguration.class.isAssignableFrom(this.queryMappingConfiguration.getClass())) {
			throw new IllegalStateException("AggregateResultSetExtractor supports with EntityQueryMappingConfiguration. "
				+ "queryMappingConfiguration: " + this.queryMappingConfiguration.getClass());
		}

		return ((EntityQueryMappingConfiguration)this.queryMappingConfiguration).getAggregateResultSetExtractor(entityType);
	}

	public String columns(Class<?> entityType) {
		return this.sqlProvider.columns(entityType);
	}

	public String aggregateColumns(Class<?> entityType) {
		return this.sqlProvider.aggregateColumns(entityType);
	}

	public BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		return this.sqlParameterSourceFactory.beanParameterSource(bean);
	}

	public MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		return this.sqlParameterSourceFactory.mapParameterSource(map);
	}

	public SqlParameterSource entityParameterSource(Object entity) {
		return this.sqlParameterSourceFactory.entityParameterSource(entity);
	}

	public CompositeSqlParameterSource compositeSqlParameterSource(SqlParameterSource... sqlParameterSources) {
		return new CompositeSqlParameterSource(sqlParameterSources);
	}
}
