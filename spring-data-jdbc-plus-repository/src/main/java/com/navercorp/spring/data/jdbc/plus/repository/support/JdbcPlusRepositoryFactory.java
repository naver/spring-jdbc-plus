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

package com.navercorp.spring.data.jdbc.plus.repository.support;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactory;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.core.RepositoryInformation;
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
	private final RelationalMappingContext context;
	private final JdbcConverter converter;
	private final ApplicationEventPublisher publisher;
	private final DataAccessStrategy accessStrategy;
	private EntityCallbacks entityCallbacks;

	/**
	 * Instantiates a new Jdbc plus repository factory.
	 *
	 * @param dataAccessStrategy the data access strategy
	 * @param context            the context
	 * @param converter          the converter
	 * @param dialect            the dialect
	 * @param publisher          the publisher
	 * @param operations         the operations
	 */
	public JdbcPlusRepositoryFactory(
		DataAccessStrategy dataAccessStrategy,
		RelationalMappingContext context,
		JdbcConverter converter,
		Dialect dialect,
		ApplicationEventPublisher publisher,
		NamedParameterJdbcOperations operations) {

		super(dataAccessStrategy, context, converter, dialect, publisher, operations);
		this.context = context;
		this.converter = converter;
		this.publisher = publisher;
		this.accessStrategy = dataAccessStrategy;
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation repositoryInformation) {

		JdbcAggregateTemplate template = new JdbcAggregateTemplate(
			publisher, context, converter, accessStrategy);

		JdbcPlusRepository<?, Object> repository = new JdbcPlusRepository<>(template,
			context.getRequiredPersistentEntity(repositoryInformation.getDomainType()));

		if (entityCallbacks != null) {
			template.setEntityCallbacks(entityCallbacks);
		}

		return repository;
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata repositoryMetadata) {
		return JdbcPlusRepository.class;
	}

	@Override
	public void setEntityCallbacks(EntityCallbacks entityCallbacks) {
		this.entityCallbacks = entityCallbacks;
		super.setEntityCallbacks(entityCallbacks);
	}
}
