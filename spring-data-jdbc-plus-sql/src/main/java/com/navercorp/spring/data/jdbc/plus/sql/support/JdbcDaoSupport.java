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

package com.navercorp.spring.data.jdbc.plus.sql.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.CollectionUtils;

import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultSetExtractor;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.jdbc.plus.support.parametersource.CompositeSqlParameterSource;

/**
 * The type Jdbc dao support.
 *
 * @author Myeonghyeon Lee
 */
public abstract class JdbcDaoSupport {
	private final EntityJdbcProvider entityJdbcProvider;

	/**
	 * Instantiates a new Jdbc dao support.
	 *
	 * @param entityJdbcProvider the entity jdbc provider
	 */
	protected JdbcDaoSupport(EntityJdbcProvider entityJdbcProvider) {
		this.entityJdbcProvider = entityJdbcProvider;
	}

	/**
	 * Gets entity jdbc provider.
	 *
	 * @return the entity jdbc provider
	 */
	protected EntityJdbcProvider getEntityJdbcProvider() {
		return this.entityJdbcProvider;
	}

	/**
	 * Gets jdbc operations.
	 *
	 * @return the jdbc operations
	 */
	public NamedParameterJdbcOperations getJdbcOperations() {
		return this.entityJdbcProvider.getJdbcOperations();
	}

	/**
	 * Columns string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	protected String columns(Class<?> entityType) {
		return this.entityJdbcProvider.columns(entityType);
	}

	/**
	 * Aggregate columns string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	protected String aggregateColumns(Class<?> entityType) {
		return this.entityJdbcProvider.aggregateColumns(entityType);
	}

	/**
	 * Tables string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	protected String tables(Class<?> entityType) {
		return this.entityJdbcProvider.tables(entityType);
	}

	/**
	 * Aggregate tables string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	protected String aggregateTables(Class<?> entityType) {
		return this.entityJdbcProvider.aggregateTables(entityType);
	}

	/**
	 * Bean parameter source bean property sql parameter source.
	 *
	 * @param bean the bean
	 * @return the bean property sql parameter source
	 */
	protected BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		return this.entityJdbcProvider.beanParameterSource(bean);
	}

	/**
	 * Bean parameter source bean property sql parameter source.
	 *
	 * @param prefix the prefix
	 * @param bean   the bean
	 * @return the bean property sql parameter source
	 */
	protected BeanPropertySqlParameterSource beanParameterSource(String prefix, Object bean) {
		return this.entityJdbcProvider.beanParameterSource(prefix, bean);
	}

	/**
	 * Map parameter source map sql parameter source.
	 *
	 * @return the map sql parameter source
	 */
	protected MapSqlParameterSource mapParameterSource() {
		return this.entityJdbcProvider.mapParameterSource(new HashMap<>());
	}

	/**
	 * Map parameter source map sql parameter source.
	 *
	 * @param map the map
	 * @return the map sql parameter source
	 */
	protected MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		return this.entityJdbcProvider.mapParameterSource(map);
	}

	/**
	 * Entity parameter source sql parameter source.
	 *
	 * @param entity the entity
	 * @return the sql parameter source
	 */
	protected SqlParameterSource entityParameterSource(Object entity) {
		return this.entityJdbcProvider.entityParameterSource(entity);
	}

	/**
	 * Composite sql parameter source composite sql parameter source.
	 *
	 * @param sqlParameterSources the sql parameter sources
	 * @return the composite sql parameter source
	 */
	protected CompositeSqlParameterSource compositeSqlParameterSource(SqlParameterSource... sqlParameterSources) {
		return this.entityJdbcProvider.compositeSqlParameterSource(sqlParameterSources);
	}

	/**
	 * Gets row mapper.
	 *
	 * @param <R>        the type parameter
	 * @param returnType the return type
	 * @return the row mapper
	 */
	protected <R> RowMapper<R> getRowMapper(Class<R> returnType) {
		return this.entityJdbcProvider.getRowMapper(returnType);
	}

	/**
	 * Gets aggregate result set extractor.
	 *
	 * @param <R>        the type parameter
	 * @param returnType the return type
	 * @return the aggregate result set extractor
	 */
	protected <R> AggregateResultSetExtractor<R> getAggregateResultSetExtractor(Class<R> returnType) {
		return this.entityJdbcProvider.getAggregateResultSetExtractor(returnType);
	}

	/**
	 * Gets application event publisher.
	 *
	 * @return the application event publisher
	 */
	protected ApplicationEventPublisher getApplicationEventPublisher() {
		return this.entityJdbcProvider.getApplicationEventPublisher();
	}

	/**
	 * Gets entity callbacks.
	 *
	 * @return the entity callbacks
	 */
	protected EntityCallbacks getEntityCallbacks() {
		return this.entityJdbcProvider.getEntityCallbacks();
	}

	/**
	 * Sqls s.
	 *
	 * @param <S>      the type parameter
	 * @param supplier the supplier
	 * @return the s
	 */
	protected <S extends SqlAware> S sqls(Supplier<S> supplier) {
		S sqls = supplier.get();
		sqls.setSql(this.entityJdbcProvider.getSqlProvider());
		return sqls;
	}

	/**
	 * Select list.
	 *
	 * @param <R>        the type parameter
	 * @param sql        the sql
	 * @param params     the params
	 * @param returnType the return type
	 * @return the list
	 */
	protected <R> List<R> select(String sql, SqlParameterSource params, Class<R> returnType) {
		RowMapper<R> rowMapper = this.getRowMapper(returnType);
		return this.select(sql, params, rowMapper);
	}

	/**
	 * Select list.
	 *
	 * @param <R>       the type parameter
	 * @param sql       the sql
	 * @param params    the params
	 * @param rowMapper the row mapper
	 * @return the list
	 */
	protected <R> List<R> select(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		return this.getJdbcOperations().query(sql, params, rowMapper);
	}

	/**
	 * Select list.
	 *
	 * @param <R>                the type parameter
	 * @param sql                the sql
	 * @param params             the params
	 * @param resultSetExtractor the result set extractor
	 * @return the list
	 */
	protected <R> List<R> select(
		String sql,
		SqlParameterSource params,
		ResultSetExtractor<List<R>> resultSetExtractor
	) {
		return this.getJdbcOperations().query(sql, params, resultSetExtractor);
	}

	/**
	 * Select one optional.
	 *
	 * @param <R>        the type parameter
	 * @param sql        the sql
	 * @param params     the params
	 * @param returnType the return type
	 * @return the optional
	 */
	protected <R> Optional<R> selectOne(String sql, SqlParameterSource params, Class<R> returnType) {
		RowMapper<R> rowMapper = this.getRowMapper(returnType);
		return this.selectOne(sql, params, rowMapper);
	}

	/**
	 * Select one optional.
	 *
	 * @param <R>       the type parameter
	 * @param sql       the sql
	 * @param params    the params
	 * @param rowMapper the row mapper
	 * @return the optional
	 */
	protected <R> Optional<R> selectOne(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		List<R> list = this.select(sql, params, rowMapper);
		if (CollectionUtils.isEmpty(list)) {
			return Optional.empty();
		}

		if (list.size() > 1) {
			String message = "Result size must be one or zero for findOne operation. result size: "
				+ list.size()
				+ ", sql: "
				+ sql
				+ ", params:"
				+ params;
			throw new IncorrectResultSizeDataAccessException(message, 1, list.size());
		}
		return Optional.of(list.get(0));
	}

	/**
	 * Select one optional.
	 *
	 * @param <R>                the type parameter
	 * @param sql                the sql
	 * @param params             the params
	 * @param resultSetExtractor the result set extractor
	 * @return the optional
	 */
	protected <R> Optional<R> selectOne(
		String sql,
		SqlParameterSource params,
		ResultSetExtractor<List<R>> resultSetExtractor
	) {
		List<R> list = this.select(sql, params, resultSetExtractor);
		if (CollectionUtils.isEmpty(list)) {
			return Optional.empty();
		}

		if (list.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, list.size());
		}
		return Optional.of(list.get(0));
	}

	/**
	 * Required one r.
	 *
	 * @param <R>        the type parameter
	 * @param sql        the sql
	 * @param params     the params
	 * @param returnType the return type
	 * @return the r
	 */
	protected <R> R requiredOne(String sql, SqlParameterSource params, Class<R> returnType) {
		RowMapper<R> rowMapper = this.getRowMapper(returnType);
		return this.requiredOne(sql, params, rowMapper);
	}

	/**
	 * Required one r.
	 *
	 * @param <R>       the type parameter
	 * @param sql       the sql
	 * @param params    the params
	 * @param rowMapper the row mapper
	 * @return the r
	 */
	protected <R> R requiredOne(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		return this.selectOne(sql, params, rowMapper)
			.orElseThrow(() -> new EmptyResultDataAccessException("RequiredOne result must be One.", 1));
	}

	/**
	 * Required one r.
	 *
	 * @param <R>                the type parameter
	 * @param sql                the sql
	 * @param params             the params
	 * @param resultSetExtractor the result set extractor
	 * @return the r
	 */
	protected <R> R requiredOne(
		String sql,
		SqlParameterSource params,
		ResultSetExtractor<List<R>> resultSetExtractor
	) {
		return this.selectOne(sql, params, resultSetExtractor)
			.orElseThrow(() -> new EmptyResultDataAccessException("RequiredOne result must be One.", 1));
	}
}
