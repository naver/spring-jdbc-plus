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

package com.navercorp.spring.data.jdbc.plus.sql.support;

import static java.util.stream.Collectors.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.conversion.MutableAggregateChange;
import org.springframework.data.relational.core.mapping.event.AfterConvertCallback;
import org.springframework.data.relational.core.mapping.event.AfterConvertEvent;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;
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
 * The type Jdbc repository support.
 *
 * @param <T>  the type parameter
 * @author Myeonghyeon Lee
 */
public abstract class JdbcRepositorySupport<T> {
	private final Class<T> entityType;
	private final EntityJdbcProvider entityJdbcProvider;

	/**
	 * Instantiates a new Jdbc repository support.
	 *
	 * @param entityType the entity type
	 * @param entityJdbcProvider the entity jdbc provider
	 */
	protected JdbcRepositorySupport(Class<T> entityType, EntityJdbcProvider entityJdbcProvider) {
		this.entityType = entityType;
		this.entityJdbcProvider = entityJdbcProvider;
	}

	/**
	 * Gets entity type.
	 *
	 * @return the entity type
	 */
	protected Class<T> getEntityType() {
		return this.entityType;
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
	 * @return the string
	 */
	protected String columns() {
		return this.columns(this.entityType);
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
	 * @return the string
	 */
	protected String aggregateColumns() {
		return this.aggregateColumns(this.entityType);
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
	 * @return the string
	 */
	protected String tables() {
		return this.tables(this.entityType);
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
	 * @return the string
	 */
	protected String aggregateTables() {
		return this.aggregateTables(this.entityType);
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
	 * @return the row mapper
	 */
	protected RowMapper<T> getRowMapper() {
		return this.getRowMapper(this.entityType);
	}

	/**
	 * Gets row mapper.
	 *
	 * @param <R>         the type parameter
	 * @param returnType the return type
	 * @return the row mapper
	 */
	protected <R> RowMapper<R> getRowMapper(Class<R> returnType) {
		return this.entityJdbcProvider.getRowMapper(returnType);
	}

	/**
	 * Gets aggregate result set extractor.
	 *
	 * @param <R>         the type parameter
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
	 * @param <S>       the type parameter
	 * @param supplier the supplier
	 * @return the s
	 */
	protected <S extends SqlAware> S sqls(Supplier<S> supplier) {
		S sqls = supplier.get();
		sqls.setSql(this.entityJdbcProvider.getSqlProvider());
		return sqls;
	}

	/**
	 * Find list.
	 *
	 * @param sql the sql
	 * @param params the params
	 * @return the list
	 */
	protected List<T> find(String sql, SqlParameterSource params) {
		return this.find(sql, params, this.entityType).stream()
			.map(this::triggerAfterConvert)
			.collect(toList());
	}

	/**
	 * Find list.
	 *
	 * @param <R>         the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param returnType the return type
	 * @return the list
	 */
	protected <R> List<R> find(String sql, SqlParameterSource params, Class<R> returnType) {
		AggregateResultSetExtractor<R> resultSetExtractor = this.getAggregateResultSetExtractor(returnType);
		return this.find(sql, params, resultSetExtractor).stream()
			.map(this::triggerAfterConvert)
			.collect(toList());
	}

	/**
	 * Find list.
	 *
	 * @param <R>        the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param rowMapper the row mapper
	 * @return the list
	 */
	protected <R> List<R> find(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		return this.getJdbcOperations().query(sql, params, rowMapper).stream()
			.map(this::triggerAfterConvert)
			.collect(toList());
	}

	/**
	 * Find list.
	 *
	 * @param <R>                 the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param resultSetExtractor the result set extractor
	 * @return the list
	 */
	protected <R> List<R> find(
		String sql,
		SqlParameterSource params,
		ResultSetExtractor<List<R>> resultSetExtractor
	) {
		return this.getJdbcOperations().query(sql, params, resultSetExtractor).stream()
			.map(this::triggerAfterConvert)
			.collect(toList());
	}

	/**
	 * Find one optional.
	 *
	 * @param sql the sql
	 * @param params the params
	 * @return the optional
	 */
	protected Optional<T> findOne(String sql, SqlParameterSource params) {
		return this.findOne(sql, params, this.entityType);
	}

	/**
	 * Find one optional.
	 *
	 * @param <R>         the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param returnType the return type
	 * @return the optional
	 */
	protected <R> Optional<R> findOne(String sql, SqlParameterSource params, Class<R> returnType) {
		AggregateResultSetExtractor<R> resultSetExtractor = this.getAggregateResultSetExtractor(returnType);
		return this.findOne(sql, params, resultSetExtractor);
	}

	/**
	 * Find one optional.
	 *
	 * @param <R>        the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param rowMapper the row mapper
	 * @return the optional
	 */
	protected <R> Optional<R> findOne(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		List<R> list = this.find(sql, params, rowMapper);
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
		return Optional.ofNullable(this.triggerAfterConvert(list.get(0)));
	}

	/**
	 * Find one optional.
	 *
	 * @param <R>                 the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param resultSetExtractor the result set extractor
	 * @return the optional
	 */
	protected <R> Optional<R> findOne(
		String sql,
		SqlParameterSource params,
		ResultSetExtractor<List<R>> resultSetExtractor) {

		List<R> list = this.find(sql, params, resultSetExtractor);
		if (CollectionUtils.isEmpty(list)) {
			return Optional.empty();
		}

		if (list.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, list.size());
		}
		return Optional.ofNullable(this.triggerAfterConvert(list.get(0)));
	}

	/**
	 * Required one t.
	 *
	 * @param sql the sql
	 * @param params the params
	 * @return the t
	 */
	protected T requiredOne(String sql, SqlParameterSource params) {
		return this.requiredOne(sql, params, this.entityType);
	}

	/**
	 * Required one r.
	 *
	 * @param <R>         the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param returnType the return type
	 * @return the r
	 */
	protected <R> R requiredOne(String sql, SqlParameterSource params, Class<R> returnType) {
		AggregateResultSetExtractor<R> resultSetExtractor = this.getAggregateResultSetExtractor(returnType);
		return this.requiredOne(sql, params, resultSetExtractor);
	}

	/**
	 * Required one r.
	 *
	 * @param <R>        the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param rowMapper the row mapper
	 * @return the r
	 */
	protected <R> R requiredOne(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		return this.findOne(sql, params, rowMapper)
			.orElseThrow(() -> new EmptyResultDataAccessException("RequiredOne result must be One.", 1));
	}

	/**
	 * Required one r.
	 *
	 * @param <R>                 the type parameter
	 * @param sql the sql
	 * @param params the params
	 * @param resultSetExtractor the result set extractor
	 * @return the r
	 */
	protected <R> R requiredOne(
		String sql,
		SqlParameterSource params,
		ResultSetExtractor<List<R>> resultSetExtractor
	) {
		return this.findOne(sql, params, resultSetExtractor)
			.orElseThrow(() -> new EmptyResultDataAccessException("RequiredOne result must be One.", 1));
	}

	/**
	 * Save one r
	 *
	 * @param sql the sql
	 * @param aggregate the aggregate for save
	 * @return affected row count
	 * @param <R> the type parameter
	 */
	protected <R> int saveOne(String sql, R aggregate) {
		return this.getJdbcOperations().update(
			sql,
			beanParameterSource(triggerBeforeSave(aggregate))
		);
	}

	/**
	 * Save list
	 *
	 * @param sql the sql
	 * @param aggregate the aggregate list for save
	 * @return affected row count
	 * @param <R> the type parameter
	 */
	protected <R> int[] saveBatch(String sql, List<R> aggregate) {
		return this.getJdbcOperations().batchUpdate(
			sql,
			aggregate.stream().map(it -> beanParameterSource(triggerBeforeSave(it))).toArray(SqlParameterSource[]::new)
		);
	}

	/**
	 * Trigger before save r
	 *
	 * @param aggregate the aggregate
	 * @return the r returned by entity callbacks
	 * @param <R> the type parameter
	 */
	protected <R> R triggerBeforeSave(R aggregate) {
		this.getApplicationEventPublisher()
			.publishEvent(new BeforeSaveEvent<>(aggregate, MutableAggregateChange.forSave(aggregate)));

		return this.getEntityCallbacks()
			.callback(BeforeSaveCallback.class, aggregate, MutableAggregateChange.forSave(aggregate));
	}

	/**
	 * Trigger after load r.
	 *
	 * @param <R>  the type parameter
	 * @param aggregate the aggregate
	 * @return the r
	 */
	protected <R> R triggerAfterConvert(R aggregate) {
		this.getApplicationEventPublisher()
			.publishEvent(new AfterConvertEvent<>(aggregate));

		return this.getEntityCallbacks()
			.callback(AfterConvertCallback.class, aggregate);
	}
}
