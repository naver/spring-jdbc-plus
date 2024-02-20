package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.jdbc.core.convert.SqlGeneratorSource;
import org.springframework.data.jdbc.core.convert.DefaultDataAccessStrategy;
import org.springframework.data.jdbc.core.convert.InsertStrategyFactory;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.SqlParametersFactory;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.Assert;

public class SoftDeleteDataAccessStrategy extends DefaultDataAccessStrategy {
	private final JdbcConverter converter;
	private final RelationalMappingContext context;
	private final NamedParameterJdbcOperations operations;
	private final PlusSqlGeneratorSource sqlGeneratorSource;
	private final SqlParametersFactory sqlParametersFactory;

	public SoftDeleteDataAccessStrategy(
		SqlGeneratorSource sqlGeneratorSource,
		RelationalMappingContext context,
		JdbcConverter converter,
		NamedParameterJdbcOperations operations,
		SqlParametersFactory sqlParametersFactory,
		InsertStrategyFactory insertStrategyFactory,
		PlusSqlGeneratorSource plusSqlGeneratorSource
	) {
		super(sqlGeneratorSource, context, converter, operations, sqlParametersFactory, insertStrategyFactory);

		Assert.notNull(sqlGeneratorSource, "SqlGeneratorSource must not be null");
		Assert.notNull(context, "RelationalMappingContext must not be null");
		Assert.notNull(converter, "JdbcConverter must not be null");
		Assert.notNull(operations, "NamedParameterJdbcOperations must not be null");
		Assert.notNull(sqlParametersFactory, "SqlParametersFactory must not be null");
		Assert.notNull(insertStrategyFactory, "InsertStrategyFactory must not be null");

		this.sqlGeneratorSource = plusSqlGeneratorSource;
		this.context = context;
		this.converter = converter;
		this.operations = operations;
		this.sqlParametersFactory = sqlParametersFactory;
	}

	@Override
	public void delete(Object id, Class<?> domainType) {
		super.delete(id, domainType);

		sqlGeneratorSource.deleteById(domainType);
	}
}
