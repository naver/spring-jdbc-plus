package com.navercorp.spring.data.jdbc.plus.sql.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.navercorp.spring.jdbc.plus.support.parametersource.CompositeSqlParameterSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.CollectionUtils;

import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultSetExtractor;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;

public abstract class JdbcDaoSupport {
	private final EntityJdbcProvider entityJdbcProvider;

	protected JdbcDaoSupport(EntityJdbcProvider entityJdbcProvider) {
		this.entityJdbcProvider = entityJdbcProvider;
	}

	protected EntityJdbcProvider getEntityJdbcProvider() {
		return this.entityJdbcProvider;
	}

	public NamedParameterJdbcOperations getJdbcOperations() {
		return this.entityJdbcProvider.getJdbcOperations();
	}

	protected String columns(Class<?> entityType) {
		return this.entityJdbcProvider.columns(entityType);
	}

	protected String aggregateColumns(Class<?> entityType) {
		return this.entityJdbcProvider.aggregateColumns(entityType);
	}

	protected BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		return this.entityJdbcProvider.beanParameterSource(bean);
	}

	protected MapSqlParameterSource mapParameterSource() {
		return this.entityJdbcProvider.mapParameterSource(new HashMap<>());
	}

	protected MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		return this.entityJdbcProvider.mapParameterSource(map);
	}

	protected SqlParameterSource entityParameterSource(Object entity) {
		return this.entityJdbcProvider.entityParameterSource(entity);
	}

	protected CompositeSqlParameterSource compositeSqlParameterSource(SqlParameterSource... sqlParameterSources) {
		return this.entityJdbcProvider.compositeSqlParameterSource(sqlParameterSources);
	}

	protected <R> RowMapper<R> getRowMapper(Class<R> returnType) {
		return this.entityJdbcProvider.getRowMapper(returnType);
	}

	protected <R> AggregateResultSetExtractor<R> getAggregateResultSetExtractor(Class<R> returnType) {
		return this.entityJdbcProvider.getAggregateResultSetExtractor(returnType);
	}

	protected <S extends SqlAware> S sqls(Supplier<S> supplier) {
		S sqls = supplier.get();
		sqls.setSql(this.entityJdbcProvider.getSqlProvider());
		return sqls;
	}

	protected <R> List<R> select(String sql, SqlParameterSource params, Class<R> returnType) {
		RowMapper<R> rowMapper = this.getRowMapper(returnType);
		return this.select(sql, params, rowMapper);
	}

	protected <R> List<R> select(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		return this.getJdbcOperations().query(sql, params, rowMapper);
	}

	protected <R> List<R> select(String sql, SqlParameterSource params, ResultSetExtractor<List<R>> resultSetExtractor) {
		return this.getJdbcOperations().query(sql, params, resultSetExtractor);
	}

	protected <R> Optional<R> selectOne(String sql, SqlParameterSource params, Class<R> returnType) {
		RowMapper<R> rowMapper = this.getRowMapper(returnType);
		return this.selectOne(sql, params, rowMapper);
	}

	protected <R> Optional<R> selectOne(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		List<R> list = this.select(sql, params, rowMapper);
		if (CollectionUtils.isEmpty(list)) {
			return Optional.empty();
		}

		if (list.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, list.size());
		}
		return Optional.ofNullable(list.get(0));
	}

	protected <R> Optional<R> selectOne(String sql, SqlParameterSource params, ResultSetExtractor<List<R>> resultSetExtractor) {
		List<R> list = this.select(sql, params, resultSetExtractor);
		if (CollectionUtils.isEmpty(list)) {
			return Optional.empty();
		}

		if (list.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, list.size());
		}
		return Optional.ofNullable(list.get(0));
	}

	protected <R> R requiredOne(String sql, SqlParameterSource params, Class<R> returnType) {
		RowMapper<R> rowMapper = this.getRowMapper(returnType);
		return this.requiredOne(sql, params, rowMapper);
	}

	protected <R> R requiredOne(String sql, SqlParameterSource params, RowMapper<R> rowMapper) {
		return this.selectOne(sql, params, rowMapper)
			.orElseThrow(() -> new EmptyResultDataAccessException("RequiredOne result must be One.", 1));
	}

	protected <R> R requiredOne(String sql, SqlParameterSource params, ResultSetExtractor<List<R>> resultSetExtractor) {
		return this.selectOne(sql, params, resultSetExtractor)
			.orElseThrow(() -> new EmptyResultDataAccessException("RequiredOne result must be One.", 1));
	}
}
