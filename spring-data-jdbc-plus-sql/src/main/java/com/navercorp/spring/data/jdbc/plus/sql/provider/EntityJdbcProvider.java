/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2025 NAVER Corp.
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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultSetExtractor;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.EntityConvertibleSqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.support.convert.SqlProvider;
import com.navercorp.spring.jdbc.plus.support.parametersource.CompositeSqlParameterSource;

/**
 * The type Entity jdbc provider.
 *
 * @author Myeonghyeon Lee
 */
public class EntityJdbcProvider {
	private final NamedParameterJdbcOperations jdbcOperations;
	private final SqlProvider sqlProvider;
	private final SqlParameterSourceFactory sqlParameterSourceFactory;
	private final QueryMappingConfiguration queryMappingConfiguration;
	private final ApplicationEventPublisher publisher;
	private final EntityCallbacks entityCallbacks;

	/**
	 * Instantiates a new Entity jdbc provider.
	 *
	 * @param jdbcOperations the jdbc operations
	 * @param sqlProvider the sql provider
	 * @param sqlParameterSourceFactory the sql parameter source factory
	 * @param queryMappingConfiguration the query mapping configuration
	 * @param publisher the publisher
	 * @param entityCallbacks the entity callbacks
	 */
	public EntityJdbcProvider(
		NamedParameterJdbcOperations jdbcOperations,
		SqlProvider sqlProvider,
		SqlParameterSourceFactory sqlParameterSourceFactory,
		QueryMappingConfiguration queryMappingConfiguration,
		ApplicationEventPublisher publisher,
		EntityCallbacks entityCallbacks
	) {
		this.jdbcOperations = jdbcOperations;
		this.sqlProvider = sqlProvider;
		this.sqlParameterSourceFactory = sqlParameterSourceFactory;
		this.queryMappingConfiguration = queryMappingConfiguration;
		this.publisher = publisher;
		this.entityCallbacks = entityCallbacks;
	}

	/**
	 * Gets jdbc operations.
	 *
	 * @return the jdbc operations
	 */
	public NamedParameterJdbcOperations getJdbcOperations() {
		return this.jdbcOperations;
	}

	/**
	 * Gets sql provider.
	 *
	 * @return the sql provider
	 */
	public SqlProvider getSqlProvider() {
		return this.sqlProvider;
	}

	/**
	 * Gets sql parameter source factory.
	 *
	 * @return the sql parameter source factory
	 */
	public SqlParameterSourceFactory getSqlParameterSourceFactory() {
		return this.sqlParameterSourceFactory;
	}

	/**
	 * Gets query mapping configuration.
	 *
	 * @return the query mapping configuration
	 */
	public QueryMappingConfiguration getQueryMappingConfiguration() {
		return this.queryMappingConfiguration;
	}

	/**
	 * Gets row mapper.
	 *
	 * @param <T>           the type parameter
	 * @param entityType the entity type
	 * @return the row mapper
	 */
	@SuppressWarnings("unchecked")
	public <T> RowMapper<T> getRowMapper(Class<T> entityType) {
		try {
			return (RowMapper<T>)this.queryMappingConfiguration.getRowMapper(entityType);
		} catch (MappingException e) {
			throw new IllegalReturnTypeException(
				"EntityRowMapper returnType must be entity type. returnType: " + entityType, e);
		}
	}

	/**
	 * Gets aggregate result set extractor.
	 *
	 * @param <T>           the type parameter
	 * @param entityType the entity type
	 * @return the aggregate result set extractor
	 */
	public <T> AggregateResultSetExtractor<T> getAggregateResultSetExtractor(Class<T> entityType) {
		if (this.queryMappingConfiguration instanceof EntityQueryMappingConfiguration entityQueryMappingConfiguration) {
			return entityQueryMappingConfiguration.getAggregateResultSetExtractor(entityType);
		} else {
			throw new IllegalStateException(
				"AggregateResultSetExtractor supports with EntityQueryMappingConfiguration. "
					+ "queryMappingConfiguration: "
					+ this.queryMappingConfiguration.getClass());
		}
	}

	/**
	 * Columns string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String columns(Class<?> entityType) {
		return this.sqlProvider.columns(entityType);
	}

	/**
	 * Aggregate columns string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String aggregateColumns(Class<?> entityType) {
		return this.sqlProvider.aggregateColumns(entityType);
	}

	/**
	 * Tables string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String tables(Class<?> entityType) {
		return this.sqlProvider.tables(entityType);
	}

	/**
	 * Aggregate tables string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String aggregateTables(Class<?> entityType) {
		return this.sqlProvider.aggregateTables(entityType);
	}

	/**
	 * Bean parameter source bean property sql parameter source.
	 *
	 * @param bean the bean
	 * @return the bean property sql parameter source
	 */
	public BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		return this.sqlParameterSourceFactory.beanParameterSource(bean);
	}

	/**
	 * Bean parameter source bean property sql parameter source.
	 *
	 * @param prefix the prefix
	 * @param bean   the bean
	 * @return the bean property sql parameter source
	 */
	public BeanPropertySqlParameterSource beanParameterSource(String prefix, Object bean) {
		if (this.sqlParameterSourceFactory instanceof EntityConvertibleSqlParameterSourceFactory convertible) {
			return convertible.beanParameterSource(prefix, bean);
		} else {
			throw new UnsupportedOperationException("Prefix saving is not supported as default.");
		}
	}

	/**
	 * Map parameter source map sql parameter source.
	 *
	 * @param map the map
	 * @return the map sql parameter source
	 */
	public MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		return this.sqlParameterSourceFactory.mapParameterSource(map);
	}

	/**
	 * Entity parameter source sql parameter source.
	 *
	 * @param entity the entity
	 * @return the sql parameter source
	 */
	public SqlParameterSource entityParameterSource(Object entity) {
		return this.sqlParameterSourceFactory.entityParameterSource(entity);
	}

	/**
	 * Composite sql parameter source composite sql parameter source.
	 *
	 * @param sqlParameterSources the sql parameter sources
	 * @return the composite sql parameter source
	 */
	public CompositeSqlParameterSource compositeSqlParameterSource(SqlParameterSource... sqlParameterSources) {
		return new CompositeSqlParameterSource(sqlParameterSources);
	}

	/**
	 * Gets application event publisher.
	 *
	 * @return the application event publisher
	 */
	public ApplicationEventPublisher getApplicationEventPublisher() {
		return this.publisher;
	}

	/**
	 * Gets entity callbacks.
	 *
	 * @return the entity callbacks
	 */
	public EntityCallbacks getEntityCallbacks() {
		return this.entityCallbacks;
	}
}
