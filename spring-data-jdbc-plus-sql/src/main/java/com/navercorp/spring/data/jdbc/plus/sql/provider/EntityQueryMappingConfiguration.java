package com.navercorp.spring.data.jdbc.plus.sql.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.jdbc.core.convert.EntityRowMapper;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.jdbc.core.RowMapper;

import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultJdbcConverter;
import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultSetExtractor;

public class EntityQueryMappingConfiguration implements QueryMappingConfiguration {
	private final Map<Class<?>, EntityRowMapper<?>> cachedRowMappers = new ConcurrentHashMap<>();
	private final Map<Class<?>, AggregateResultSetExtractor<?>> cachedAggregateResultSetExtractors = new ConcurrentHashMap<>();

	private final RelationalMappingContext mappingContext;
	private final JdbcConverter jdbcConverter;

	public EntityQueryMappingConfiguration(RelationalMappingContext mappingContext, JdbcConverter jdbcConverter) {
		this.mappingContext = mappingContext;
		this.jdbcConverter = jdbcConverter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> RowMapper<T> getRowMapper(Class<T> entityType) {
		return (EntityRowMapper<T>)this.cachedRowMappers.computeIfAbsent(entityType, type -> {
			RelationalPersistentEntity<T> entity = (RelationalPersistentEntity<T>)
				this.mappingContext.getRequiredPersistentEntity(entityType);
			return new EntityRowMapper<>(entity, this.jdbcConverter);
		});
	}

	@SuppressWarnings("unchecked")
	public <T> AggregateResultSetExtractor<T> getAggregateResultSetExtractor(Class<T> entityType) {
		if (!AggregateResultJdbcConverter.class.isAssignableFrom(this.jdbcConverter.getClass())) {
			throw new IllegalStateException("AggregateResultSetExtractor can support with AggregateResultJdbcConverter. jdbcConverter: "
				+ this.jdbcConverter.getClass());
		}

		return (AggregateResultSetExtractor<T>)this.cachedAggregateResultSetExtractors.computeIfAbsent(entityType, type -> {
			RelationalPersistentEntity<T> entity = (RelationalPersistentEntity<T>)
				this.mappingContext.getRequiredPersistentEntity(entityType);
			 return new AggregateResultSetExtractor<>(entity, (AggregateResultJdbcConverter) this.jdbcConverter);
		});
	}

	public RelationalMappingContext getMappingContext() {
		return this.mappingContext;
	}

	public JdbcConverter getJdbcConverter() {
		return this.jdbcConverter;
	}
}
