/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * The type Entity query mapping configuration.
 *
 * @author Myeonghyeon Lee
 */
public class EntityQueryMappingConfiguration implements QueryMappingConfiguration {
	private final Map<Class<?>, EntityRowMapper<?>> cachedRowMappers = new ConcurrentHashMap<>();
	private final Map<Class<?>, AggregateResultSetExtractor<?>> cachedAggregateResultSetExtractors
		= new ConcurrentHashMap<>();

	private final RelationalMappingContext mappingContext;
	private final JdbcConverter jdbcConverter;

	/**
	 * Instantiates a new Entity query mapping configuration.
	 *
	 * @param mappingContext the mapping context
	 * @param jdbcConverter  the jdbc converter
	 */
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

	/**
	 * Gets aggregate result set extractor.
	 *
	 * @param <T>        the type parameter
	 * @param entityType the entity type
	 * @return the aggregate result set extractor
	 */
	@SuppressWarnings("unchecked")
	public <T> AggregateResultSetExtractor<T> getAggregateResultSetExtractor(Class<T> entityType) {
		if (!AggregateResultJdbcConverter.class.isAssignableFrom(this.jdbcConverter.getClass())) {
			throw new IllegalStateException(
				"AggregateResultSetExtractor can support with AggregateResultJdbcConverter. "
					+ "jdbcConverter: " + this.jdbcConverter.getClass());
		}

		return (AggregateResultSetExtractor<T>)this.cachedAggregateResultSetExtractors.computeIfAbsent(
			entityType, type -> {
				RelationalPersistentEntity<T> entity = (RelationalPersistentEntity<T>)
					this.mappingContext.getRequiredPersistentEntity(entityType);
				return new AggregateResultSetExtractor<>(
					entity, (AggregateResultJdbcConverter)this.jdbcConverter);
			});
	}

	/**
	 * Gets mapping context.
	 *
	 * @return the mapping context
	 */
	public RelationalMappingContext getMappingContext() {
		return this.mappingContext;
	}

	/**
	 * Gets jdbc converter.
	 *
	 * @return the jdbc converter
	 */
	public JdbcConverter getJdbcConverter() {
		return this.jdbcConverter;
	}
}
