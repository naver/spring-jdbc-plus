package com.navercorp.spring.data.jdbc.plus.repository.config;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.QueryMappingConfiguration;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.navercorp.spring.data.jdbc.plus.support.convert.JdbcPlusDataAccessStrategyFactory;

/**
 * Utility class to providing factory methods for JDBC Plus infrastructure components.
 * <p>
 * Mainly for use within the framework or for configuration arrangements that require customization of configuration.
 * {@link org.springframework.data.jdbc.repository.config.JdbcConfiguration}
 *
 * @since 4.0
 */
public class JdbcPlusConfiguration {
	private JdbcPlusConfiguration() {}

	public static DataAccessStrategy createDataAccessStrategy(
		NamedParameterJdbcOperations operations,
		JdbcConverter jdbcConverter,
		@Nullable QueryMappingConfiguration mappingConfiguration,
		JdbcDialect dialect
	) {
		DataAccessStrategy delegate =
			JdbcConfiguration.createDataAccessStrategy(operations, jdbcConverter, mappingConfiguration, dialect);

		return new JdbcPlusDataAccessStrategyFactory(
			delegate,
			jdbcConverter,
			operations,
			dialect
		).create();
	}
}
