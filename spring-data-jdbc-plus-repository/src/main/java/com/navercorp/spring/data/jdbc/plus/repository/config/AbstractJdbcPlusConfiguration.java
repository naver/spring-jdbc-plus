package com.navercorp.spring.data.jdbc.plus.repository.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.navercorp.spring.data.jdbc.plus.support.convert.JdbcPlusDataAccessStrategyFactory;

public class AbstractJdbcPlusConfiguration extends AbstractJdbcConfiguration {

	/**
	 * Create a delegate {@link DataAccessStrategy} for supporting
	 * {@link com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn}.
	 *
	 * @see AbstractJdbcConfiguration#dataAccessStrategyBean
	 * @return will never be {@literal null}.
	 */
	@Override
	@Bean
	public DataAccessStrategy dataAccessStrategyBean(
		NamedParameterJdbcOperations operations,
		JdbcConverter jdbcConverter,
		JdbcMappingContext context,
		JdbcDialect dialect
	) {
		DataAccessStrategy delegate = super.dataAccessStrategyBean(operations, jdbcConverter, context, dialect);

		return new JdbcPlusDataAccessStrategyFactory(
			delegate,
			jdbcConverter,
			operations,
			dialect
		).create();
	}
}
