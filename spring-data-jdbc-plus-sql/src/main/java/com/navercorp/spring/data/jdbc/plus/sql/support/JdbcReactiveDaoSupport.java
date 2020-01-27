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
 * The type Jdbc reactive dao support.
 *
 * @author Myeonghyeon Lee
 */
public abstract class JdbcReactiveDaoSupport extends JdbcDaoSupport {
	private final JdbcReactiveTemplate jdbcReactiveTemplate;

	/**
	 * Instantiates a new Jdbc reactive dao support.
	 *
	 * @param entityJdbcProvider   the entity jdbc provider
	 * @param jdbcReactiveTemplate the jdbc reactive template
	 */
	protected JdbcReactiveDaoSupport(
		EntityJdbcProvider entityJdbcProvider,
		JdbcReactiveTemplate jdbcReactiveTemplate) {

		super(entityJdbcProvider);
		this.jdbcReactiveTemplate = jdbcReactiveTemplate;
	}

	/**
	 * Gets jdbc reactive template.
	 *
	 * @return the jdbc reactive template
	 */
	protected JdbcReactiveTemplate getJdbcReactiveTemplate() {
		return this.jdbcReactiveTemplate;
	}

	/**
	 * Select flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @return the flux
	 */
	protected <R> Flux<R> selectFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper) {

		return this.jdbcReactiveTemplate.queryFlux(sql, jdbcOperations, params, rowMapper);
	}

	/**
	 * Select flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @param scheduler      the scheduler
	 * @return the flux
	 */
	protected <R> Flux<R> selectFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper,
		Scheduler scheduler) {

		return this.jdbcReactiveTemplate.queryFlux(sql, jdbcOperations, params, rowMapper, scheduler);
	}

	/**
	 * Select flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @param queueSize      the queue size
	 * @param bufferTimeout  the buffer timeout
	 * @return the flux
	 */
	protected <R> Flux<R> selectFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper,
		int queueSize,
		long bufferTimeout) {

		return this.jdbcReactiveTemplate.queryFlux(
			sql, jdbcOperations, params, rowMapper, queueSize, bufferTimeout);
	}

	/**
	 * Select flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @param scheduler      the scheduler
	 * @param queueSize      the queue size
	 * @param bufferTimeout  the buffer timeout
	 * @return the flux
	 */
	protected <R> Flux<R> selectFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper,
		Scheduler scheduler,
		int queueSize,
		long bufferTimeout) {

		return this.jdbcReactiveTemplate.queryFlux(
			sql, jdbcOperations, params, rowMapper, scheduler, queueSize, bufferTimeout);
	}
}
