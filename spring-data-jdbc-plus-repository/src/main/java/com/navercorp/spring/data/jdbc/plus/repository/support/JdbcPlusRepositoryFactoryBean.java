package com.navercorp.spring.data.jdbc.plus.repository.support;

import java.io.Serializable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.DefaultDataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.SqlGeneratorSource;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.jdbc.repository.RowMapperMap;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactory;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.data.jdbc.repository.support.JdbcRepositoryFactoryBean}
 */
public class JdbcPlusRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
	extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> implements ApplicationEventPublisherAware {

	private ApplicationEventPublisher publisher;
	private BeanFactory beanFactory;
	private RelationalMappingContext mappingContext;
	private JdbcConverter converter;
	private DataAccessStrategy dataAccessStrategy;
	private QueryMappingConfiguration queryMappingConfiguration = QueryMappingConfiguration.EMPTY;
	private NamedParameterJdbcOperations operations;
	private EntityCallbacks entityCallbacks;
	private Dialect dialect;

	/**
	 * Creates a new {@link TransactionalRepositoryFactoryBeanSupport} for the given repository interface.
	 *
	 * @param repositoryInterface must not be {@literal null}.
	 */
	protected JdbcPlusRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
	 */
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {

		super.setApplicationEventPublisher(publisher);

		this.publisher = publisher;
	}

	/**
	 * Creates the actual {@link RepositoryFactorySupport} instance.
	 */
	@Override
	protected RepositoryFactorySupport doCreateRepositoryFactory() {

		JdbcRepositoryFactory jdbcRepositoryFactory = new JdbcPlusRepositoryFactory(
			dataAccessStrategy, mappingContext, converter, publisher, operations);
		jdbcRepositoryFactory.setQueryMappingConfiguration(queryMappingConfiguration);
		jdbcRepositoryFactory.setEntityCallbacks(entityCallbacks);

		return jdbcRepositoryFactory;
	}

	@Autowired
	protected void setMappingContext(RelationalMappingContext mappingContext) {

		super.setMappingContext(mappingContext);
		this.mappingContext = mappingContext;
	}

	@Autowired
	protected void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	/**
	 * @param dataAccessStrategy can be {@literal null}.
	 */
	public void setDataAccessStrategy(DataAccessStrategy dataAccessStrategy) {
		this.dataAccessStrategy = dataAccessStrategy;
	}

	/**
	 * @param queryMappingConfiguration can be {@literal null}. {@link #afterPropertiesSet()} defaults to
	 *          {@link QueryMappingConfiguration#EMPTY} if {@literal null}.
	 */
	@Autowired(required = false)
	public void setQueryMappingConfiguration(QueryMappingConfiguration queryMappingConfiguration) {
		this.queryMappingConfiguration = queryMappingConfiguration;
	}

	/**
	 * @param rowMapperMap can be {@literal null}. {@link #afterPropertiesSet()} defaults to
	 * 			{@link RowMapperMap#EMPTY} if {@literal null}.
	 * @deprecated use {@link #setQueryMappingConfiguration(QueryMappingConfiguration)} instead.
	 */
	@Deprecated
	@Autowired(required = false)
	public void setRowMapperMap(RowMapperMap rowMapperMap) {
		setQueryMappingConfiguration(rowMapperMap);
	}

	public void setJdbcOperations(NamedParameterJdbcOperations operations) {
		this.operations = operations;
	}

	@Autowired
	public void setConverter(JdbcConverter converter) {
		this.converter = converter;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {

		super.setBeanFactory(beanFactory);

		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		Assert.state(this.mappingContext != null, "MappingContext is required and must not be null!");
		Assert.state(this.converter != null, "RelationalConverter is required and must not be null!");

		if (this.operations == null) {

			Assert.state(beanFactory != null,
				"If no JdbcOperations are set a BeanFactory must be available.");

			this.operations = beanFactory.getBean(NamedParameterJdbcOperations.class);
		}

		if (this.dataAccessStrategy == null) {

			Assert.state(beanFactory != null,
				"If no DataAccessStrategy is set a BeanFactory must be available.");

			this.dataAccessStrategy = this.beanFactory.getBeanProvider(DataAccessStrategy.class) //
				.getIfAvailable(() -> {

					Assert.state(this.dialect != null, "Dialect is required and must not be null!");

					SqlGeneratorSource sqlGeneratorSource = new SqlGeneratorSource(this.mappingContext, this.converter, this.dialect);
					return new DefaultDataAccessStrategy(
						sqlGeneratorSource, this.mappingContext, this.converter, this.operations);
				});
		}

		if (this.queryMappingConfiguration == null) {
			this.queryMappingConfiguration = QueryMappingConfiguration.EMPTY;
		}

		if (beanFactory != null) {
			entityCallbacks = EntityCallbacks.create(beanFactory);
		}

		super.afterPropertiesSet();
	}
}
