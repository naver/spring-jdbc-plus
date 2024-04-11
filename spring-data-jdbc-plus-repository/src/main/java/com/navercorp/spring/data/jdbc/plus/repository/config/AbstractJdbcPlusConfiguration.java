package com.navercorp.spring.data.jdbc.plus.repository.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.navercorp.spring.data.jdbc.plus.support.convert.JdbcPlusDataAccessStrategyFactory;
import com.navercorp.spring.data.jdbc.plus.support.convert.SqlGeneratorSource;
import com.navercorp.spring.data.jdbc.plus.support.parametersource.SoftDeleteSqlParametersFactory;

public class AbstractJdbcPlusConfiguration extends AbstractJdbcConfiguration {

	@Override
	@Bean
	public DataAccessStrategy dataAccessStrategyBean(
		NamedParameterJdbcOperations operations,
		JdbcConverter jdbcConverter,
		JdbcMappingContext context,
		Dialect dialect
	) {
		DataAccessStrategy delegate = super.dataAccessStrategyBean(operations, jdbcConverter, context, dialect);

		SqlGeneratorSource sqlGeneratorSource = new SqlGeneratorSource(context, jdbcConverter, dialect);
		JdbcPlusDataAccessStrategyFactory factory = new JdbcPlusDataAccessStrategyFactory(
			delegate,
			jdbcConverter,
			operations,
			sqlGeneratorSource,
			new SoftDeleteSqlParametersFactory(context, jdbcConverter)
		);

		return factory.create();
	}
}
