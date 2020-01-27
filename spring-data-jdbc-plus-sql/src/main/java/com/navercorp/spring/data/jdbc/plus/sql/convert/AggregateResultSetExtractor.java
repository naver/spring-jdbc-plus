package com.navercorp.spring.data.jdbc.plus.sql.convert;

import java.sql.ResultSet;
import java.util.List;

import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.jdbc.core.ResultSetExtractor;

public class AggregateResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
	private final RelationalPersistentEntity<T> entity;
	private final AggregateResultJdbcConverter jdbcConverter;

	public AggregateResultSetExtractor(Class<T> type, AggregateResultJdbcConverter jdbcConverter) {
		this.entity = (RelationalPersistentEntity<T>)jdbcConverter.getMappingContext().getRequiredPersistentEntity(type);
		this.jdbcConverter = jdbcConverter;
	}

	public AggregateResultSetExtractor(RelationalPersistentEntity<T> entity, AggregateResultJdbcConverter jdbcConverter) {
		this.entity = entity;
		this.jdbcConverter = jdbcConverter;
	}

	@Override
	public List<T> extractData(ResultSet resultSet) {
		return this.jdbcConverter.mapAggregate(entity, resultSet);
	}
}
