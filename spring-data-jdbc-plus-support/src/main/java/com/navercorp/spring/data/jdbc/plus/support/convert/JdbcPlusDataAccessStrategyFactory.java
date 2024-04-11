/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.spring.data.jdbc.plus.support.convert;

import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.DefaultDataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.Assert;

import com.navercorp.spring.data.jdbc.plus.support.parametersource.SoftDeleteSqlParametersFactory;

/**
 * Factory to create a {@link DataAccessStrategy} based on the configuration of the provided components. Specifically,
 * this factory creates a {@link SingleQueryFallbackDataAccessStrategy} that falls back to
 * {@link DefaultDataAccessStrategy} if Single Query Loading is not supported. This factory encapsulates
 * {@link DataAccessStrategy} for consistent access strategy creation.
 *
 * @since 3.3
 */
public class JdbcPlusDataAccessStrategyFactory {

	private final DataAccessStrategy delegate;
	private final JdbcConverter converter;
	private final NamedParameterJdbcOperations operations;
	private final SqlGeneratorSource jdbcPlusSqlGeneratorSource;
	private final SoftDeleteSqlParametersFactory softDeleteSqlParametersFactory;

	public JdbcPlusDataAccessStrategyFactory(
		DataAccessStrategy delegate,
		JdbcConverter converter,
		NamedParameterJdbcOperations operations,
		SqlGeneratorSource sqlGeneratorSource,
		SoftDeleteSqlParametersFactory softDeleteSqlParametersFactory
	) {
		Assert.notNull(delegate, "DataAccessStrategy must not be null");
		Assert.notNull(converter, "JdbcConverter must not be null");
		Assert.notNull(operations, "NamedParameterJdbcOperations must not be null");
		Assert.notNull(sqlGeneratorSource, "SqlGeneratorSource must not be null");
		Assert.notNull(softDeleteSqlParametersFactory, "SoftDeleteSqlParametersFactory must not be null");

		this.delegate = delegate;
		this.converter = converter;
		this.operations = operations;
		this.jdbcPlusSqlGeneratorSource = sqlGeneratorSource;
		this.softDeleteSqlParametersFactory = softDeleteSqlParametersFactory;
	}

	public DataAccessStrategy create() {
		return new JdbcPlusDataAccessStrategy(
			delegate,
			this.converter.getMappingContext(),
			this.operations,
			jdbcPlusSqlGeneratorSource,
			softDeleteSqlParametersFactory
		);
	}
}
