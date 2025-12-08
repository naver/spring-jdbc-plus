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

package com.navercorp.spring.data.jdbc.plus.repository.support;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactory;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

/**
 * Creates repository implementation based on JDBC.
 *
 * @author Myeonghyeon Lee
 *
 * {@link JdbcRepositoryFactory}
 */
public class JdbcPlusRepositoryFactory extends JdbcRepositoryFactory {

	/**
	 * Instantiates a new Jdbc plus repository factory.
	 *
	 * @param jdbcAggregateOperations the jdbc aggregate operations
	 */
	public JdbcPlusRepositoryFactory(JdbcAggregateOperations jdbcAggregateOperations) {
		super(jdbcAggregateOperations);
	}

	/**
	 * Instantiates a new Jdbc plus repository factory.
	 *
	 * @param dataAccessStrategy the data access strategy
	 * @param context            the context
	 * @param converter          the converter
	 * @param dialect            the dialect
	 * @param publisher          the publisher
	 * @param jdbcOperations     the operations
	 */
	@Deprecated(since = "4.0", forRemoval = true)
	public JdbcPlusRepositoryFactory(
		DataAccessStrategy dataAccessStrategy,
		RelationalMappingContext context,
		JdbcConverter converter,
		Dialect dialect,
		ApplicationEventPublisher publisher,
		NamedParameterJdbcOperations jdbcOperations
	) {
		super(dataAccessStrategy, context, converter, dialect, publisher, jdbcOperations);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata repositoryMetadata) {
		return JdbcPlusRepository.class;
	}
}
