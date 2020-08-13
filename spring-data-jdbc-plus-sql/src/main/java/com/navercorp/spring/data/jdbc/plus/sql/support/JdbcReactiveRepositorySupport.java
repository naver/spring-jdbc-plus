/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
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

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.template.JdbcReactiveTemplate;

/**
 * The type Jdbc reactive repository support.
 *
 * @param <T> the type parameter
 * @author Sanha Lee
 */
public abstract class JdbcReactiveRepositorySupport<T> extends JdbcRepositorySupport<T> {
	private final JdbcReactiveTemplate jdbcReactiveTemplate;

	/**
	 * Instantiates a new Jdbc reactive repository support.
	 *
	 * @param entityType           the entity type
	 * @param entityJdbcProvider   the entity jdbc provider
	 * @param jdbcReactiveTemplate the jdbc reactive template
	 */
	protected JdbcReactiveRepositorySupport(
		Class<T> entityType,
		EntityJdbcProvider entityJdbcProvider,
		JdbcReactiveTemplate jdbcReactiveTemplate) {

		super(entityType, entityJdbcProvider);
		this.jdbcReactiveTemplate = jdbcReactiveTemplate;
	}

	/**
	 * Get the Jdbc reactive template.
	 *
	 * @return the Jdbc reactive template
	 */
	protected JdbcReactiveTemplate getJdbcReactiveTemplate() {
		return this.jdbcReactiveTemplate;
	}

	/**
	 * Find all matched elements as a flux.
	 *
	 * @param sql    the sql query to execute
	 * @param params the parameters to bind to the query
	 * @return the result flux
	 */
	protected Flux<T> findFlux(
		String sql,
		SqlParameterSource params) {

		RowMapper<T> rowMapper = this.getRowMapper();
		return this.jdbcReactiveTemplate.queryFlux(
			sql, this.getEntityJdbcProvider().getJdbcOperations(), params, rowMapper);
	}

	/**
	 * Find all matched elements as a flux.
	 *
	 * @param sql       the sql query to execute
	 * @param params    the parameters to bind to the query
	 * @param rowMapper the row mapper
	 * @return the result flux
	 */
	protected Flux<T> findFlux(
		String sql,
		SqlParameterSource params,
		RowMapper<T> rowMapper) {

		return this.jdbcReactiveTemplate.queryFlux(
			sql, this.getEntityJdbcProvider().getJdbcOperations(), params, rowMapper);
	}

	/**
	 * Find all matched elements as a flux.
	 *
	 * @param sql            the sql query to execute
	 * @param jdbcOperations the Jdbc operations
	 * @param params         the parameters to bind to the query
	 * @param rowMapper      the row mapper
	 * @return the result flux
	 */
	protected Flux<T> findFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<T> rowMapper) {

		return this.jdbcReactiveTemplate.queryFlux(sql, jdbcOperations, params, rowMapper);
	}

	/**
	 * Find all matched elements as a flux.
	 *
	 * @param sql            the sql query to execute
	 * @param jdbcOperations the Jdbc operations
	 * @param params         the parameters to bind to the query
	 * @param rowMapper      the row mapper
	 * @param scheduler      the scheduler
	 * @return the result flux
	 */
	protected Flux<T> findFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<T> rowMapper,
		Scheduler scheduler) {

		return this.jdbcReactiveTemplate.queryFlux(sql, jdbcOperations, params, rowMapper, scheduler);
	}

	/**
	 * Find all matched elements as a flux.
	 *
	 * @param sql            the sql query to execute
	 * @param jdbcOperations the Jdbc operations
	 * @param params         the parameters to bind to the query
	 * @param rowMapper      the row mapper
	 * @param queueSize      the queue size
	 * @param bufferTimeout  the buffer timeout
	 * @return the result flux
	 */
	protected Flux<T> findFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<T> rowMapper,
		int queueSize,
		long bufferTimeout) {

		return this.jdbcReactiveTemplate.queryFlux(
			sql, jdbcOperations, params, rowMapper, queueSize, bufferTimeout);
	}

	/**
	 * Find all matched elements as a flux.
	 *
	 * @param sql            the sql query to execute
	 * @param jdbcOperations the Jdbc operations
	 * @param params         the parameters to bind to the query
	 * @param rowMapper      the row mapper
	 * @param scheduler      the scheduler
	 * @param queueSize      the queue size
	 * @param bufferTimeout  the buffer timeout
	 * @return the result flux
	 */
	protected Flux<T> findFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<T> rowMapper,
		Scheduler scheduler,
		int queueSize,
		long bufferTimeout) {

		return this.jdbcReactiveTemplate.queryFlux(
			sql, jdbcOperations, params, rowMapper, scheduler, queueSize, bufferTimeout);
	}
}
