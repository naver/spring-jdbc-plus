package com.navercorp.spring.data.jdbc.plus.repository.support;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactory;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

/**
 * {@link JdbcRepositoryFactory}
 */
public class JdbcPlusRepositoryFactory extends JdbcRepositoryFactory {
	private final RelationalMappingContext context;
	private final JdbcConverter converter;
	private final ApplicationEventPublisher publisher;
	private final DataAccessStrategy accessStrategy;
	private EntityCallbacks entityCallbacks;

	public JdbcPlusRepositoryFactory(
		DataAccessStrategy dataAccessStrategy,
		RelationalMappingContext context,
		JdbcConverter converter,
		ApplicationEventPublisher publisher,
		NamedParameterJdbcOperations operations) {

		super(dataAccessStrategy, context, converter, publisher, operations);
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
