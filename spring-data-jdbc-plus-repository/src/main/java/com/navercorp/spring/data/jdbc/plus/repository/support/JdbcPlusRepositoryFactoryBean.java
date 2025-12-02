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

import java.io.Serializable;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.DataAccessStrategyFactory;
import org.springframework.data.jdbc.core.convert.InsertStrategyFactory;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.QueryMappingConfiguration;
import org.springframework.data.jdbc.core.convert.SqlGeneratorSource;
import org.springframework.data.jdbc.core.convert.SqlParametersFactory;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.Assert;

import com.navercorp.spring.data.jdbc.plus.support.convert.JdbcPlusDataAccessStrategyFactory;

/**
 * Special adapter for Springs {@link org.springframework.beans.factory.FactoryBean} interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @author Myeonghyeon Lee
 *
 * {@link org.springframework.data.jdbc.repository.support.JdbcRepositoryFactoryBean}
 *
 * @param <T>  the repository type parameter
 * @param <S>  the entity type parameter
 * @param <ID> the id type parameter
 */
public class JdbcPlusRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
	extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> implements ApplicationEventPublisherAware {

	private @Nullable ApplicationEventPublisher publisher;
	private @Nullable BeanFactory beanFactory;
	private @Nullable JdbcAggregateOperations aggregateOperations;
	private @Nullable NamedParameterJdbcOperations jdbcOperations;
	private @Nullable JdbcConverter converter;
	private @Nullable JdbcDialect dialect;
	private @Nullable DataAccessStrategy dataAccessStrategy;
	private EntityCallbacks entityCallbacks = EntityCallbacks.create();
	private @Nullable RelationalMappingContext mappingContext;
	private @Nullable QueryMappingConfiguration queryMappingConfiguration;

	/**
	 * Creates a new {@link TransactionalRepositoryFactoryBeanSupport} for the given repository interface.
	 *
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public JdbcPlusRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
	 * #setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
	 */
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {

		super.setApplicationEventPublisher(publisher);

		this.publisher = publisher;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {

		super.setBeanFactory(beanFactory);

		this.entityCallbacks = EntityCallbacks.create(beanFactory);
		this.beanFactory = beanFactory;
	}

	/**
	 * Set the {@link JdbcAggregateOperations} to use for this factory bean.
	 *
	 * @param jdbcAggregateOperations
	 * @since 4.0
	 */
	public void setJdbcAggregateOperations(JdbcAggregateOperations jdbcAggregateOperations) {
		Assert.notNull(jdbcAggregateOperations, "JdbcAggregateOperations must not be null");

		this.aggregateOperations = jdbcAggregateOperations;

		if (this.converter == null) {
			setConverter(jdbcAggregateOperations.getConverter());
		}

		if (this.mappingContext == null) {
			setMappingContext(jdbcAggregateOperations.getConverter().getMappingContext());
		}
	}


	/**
	 * Sets jdbc operations.
	 *
	 * @param operations the operations
	 */
	public void setJdbcOperations(NamedParameterJdbcOperations operations) {

		Assert.notNull(operations, "NamedParameterJdbcOperations must not be null");

		this.jdbcOperations = operations;
	}

	/**
	 * Sets converter.
	 *
	 * @param converter the converter
	 */
	public void setConverter(JdbcConverter converter) {

		Assert.notNull(converter, "JdbcConverter must not be null");

		this.converter = converter;
	}

	/**
	 * Sets dialect.
	 *
	 * @param dialect the dialect
	 */
	public void setDialect(JdbcDialect dialect) {

		Assert.notNull(dialect, "Dialect must not be null");

		this.dialect = dialect;
	}

	/**
	 * Sets data access strategy.
	 *
	 * @param dataAccessStrategy can be {@literal null}.
	 */
	public void setDataAccessStrategy(DataAccessStrategy dataAccessStrategy) {

		Assert.notNull(dataAccessStrategy, "DataAccessStrategy must not be null");

		this.dataAccessStrategy = dataAccessStrategy;
	}

	/**
	 * Sets mapping context.
	 *
	 * @param mappingContext the mapping context
	 */
	public void setMappingContext(RelationalMappingContext mappingContext) {

		Assert.notNull(mappingContext, "MappingContext must not be null");

		super.setMappingContext(mappingContext);
		this.mappingContext = mappingContext;
	}

	/**
	 * Sets query mapping configuration.
	 *
	 * @param queryMappingConfiguration can be {@literal null}.
	 * {@link #afterPropertiesSet()} defaults to
	 * {@link QueryMappingConfiguration#EMPTY} if {@literal null}.
	 */
	public void setQueryMappingConfiguration(@Nullable QueryMappingConfiguration queryMappingConfiguration) {
		if (queryMappingConfiguration == null) {
			return;
		}

		this.queryMappingConfiguration = queryMappingConfiguration;
	}

	/**
	 * Creates the actual {@link RepositoryFactorySupport} instance.
	 */
	@Override
	protected RepositoryFactorySupport doCreateRepositoryFactory() {

		Assert.state(this.queryMappingConfiguration != null, "RelationalConverter is required and must not be null");
		Assert.state(this.publisher != null, "ApplicationEventPublisher is required and must not be null");

		JdbcPlusRepositoryFactory repositoryFactory;

		if (this.aggregateOperations != null) {
			repositoryFactory = new JdbcPlusRepositoryFactory(this.aggregateOperations);
		} else {

			Assert.state(this.dataAccessStrategy != null, "DataAccessStrategy is required and must not be null");
			Assert.state(this.converter != null, "RelationalConverter is required and must not be null");

			JdbcAggregateOperations operations = new JdbcAggregateTemplate(converter, dataAccessStrategy);

			repositoryFactory = new JdbcPlusRepositoryFactory(operations);
			repositoryFactory.setEntityCallbacks(entityCallbacks);
		}

		repositoryFactory.setApplicationEventPublisher(this.publisher);
		repositoryFactory.setQueryMappingConfiguration(queryMappingConfiguration);
		repositoryFactory.setBeanFactory(beanFactory);

		return repositoryFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		Assert.state(this.mappingContext != null, "MappingContext is required and must not be null");
		Assert.state(this.converter != null, "RelationalConverter is required and must not be null");

		if (this.queryMappingConfiguration == null) {
			if (this.beanFactory == null) {
				this.queryMappingConfiguration = QueryMappingConfiguration.EMPTY;
			} else {

				this.queryMappingConfiguration = beanFactory.getBeanProvider(QueryMappingConfiguration.class)
					.getIfAvailable(() -> QueryMappingConfiguration.EMPTY);
			}
		}

		if (this.aggregateOperations == null) {

			if (this.jdbcOperations == null && this.dataAccessStrategy == null && this.beanFactory != null) {
				JdbcAggregateOperations operations = this.beanFactory.getBeanProvider(JdbcAggregateOperations.class)
					.getIfAvailable();
				if (operations != null) {
					setJdbcAggregateOperations(operations);
				}
			}

			if (this.converter != null && this.mappingContext == null) {
				setMappingContext(this.converter.getMappingContext());
			}

			Assert.state(this.publisher != null, "ApplicationPublisher is null");
			Assert.state(this.mappingContext != null, "MappingContext is null");

			if (this.jdbcOperations == null) {

				Assert.state(this.beanFactory != null, "If no JdbcOperations are set a BeanFactory must be available");
				this.jdbcOperations = this.beanFactory.getBean(NamedParameterJdbcOperations.class);
			}

			if (this.dataAccessStrategy == null) {

				Assert.state(beanFactory != null,
					"If no DataAccessStrategy is set a BeanFactory must be available");

				this.dataAccessStrategy = this.beanFactory.getBeanProvider(DataAccessStrategy.class) //
					.getIfAvailable(() -> {

						Assert.state(this.dialect != null, "Dialect is required and must not be null");

						SqlGeneratorSource sqlGeneratorSource = new SqlGeneratorSource(
							this.mappingContext, this.converter, this.dialect);
						com.navercorp.spring.data.jdbc.plus.support.convert.SqlGeneratorSource plusSqlGeneratorSource =
							new com.navercorp.spring.data.jdbc.plus.support.convert.SqlGeneratorSource(
								this.mappingContext,
								this.converter,
								dialect
							);
						SqlParametersFactory sqlParametersFactory = new SqlParametersFactory(
							this.mappingContext, this.converter);
						InsertStrategyFactory insertStrategyFactory = new InsertStrategyFactory(this.jdbcOperations,
							this.dialect);
						DataAccessStrategy delegate = buildDataAccessStrategyDelegate(
							sqlGeneratorSource,
							sqlParametersFactory,
							insertStrategyFactory
						);

						JdbcPlusDataAccessStrategyFactory factory = new JdbcPlusDataAccessStrategyFactory(
							delegate,
							converter,
							jdbcOperations,
							dialect
						);

						return factory.create();
					});
			}
		}

		super.afterPropertiesSet();
	}

	private DataAccessStrategy buildDataAccessStrategyDelegate(
		SqlGeneratorSource sqlGeneratorSource,
		SqlParametersFactory sqlParametersFactory,
		InsertStrategyFactory insertStrategyFactory
	) {
		Assert.state(this.converter != null, "Converter must not be null");
		Assert.state(this.jdbcOperations != null, "JdbcOperations must not be null");
		Assert.state(this.queryMappingConfiguration != null, "QueryMappingConfiguration must not be null");

		return new DataAccessStrategyFactory(
			sqlGeneratorSource,
			this.converter,
			this.jdbcOperations,
			sqlParametersFactory,
			insertStrategyFactory,
			this.queryMappingConfiguration
		).create();
	}
}
