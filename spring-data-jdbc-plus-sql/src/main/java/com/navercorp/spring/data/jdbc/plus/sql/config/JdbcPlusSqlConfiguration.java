package com.navercorp.spring.data.jdbc.plus.sql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.convert.DefaultJdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultJdbcConverter;
import com.navercorp.spring.data.jdbc.plus.sql.convert.SqlProvider;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.DefaultSqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityQueryMappingConfiguration;

@Configuration
public class JdbcPlusSqlConfiguration {
	@Bean
	public SqlProvider sqlProvider(JdbcMappingContext jdbcMappingContext, JdbcConverter converter, Dialect dialect) {
		return new SqlProvider(jdbcMappingContext, converter, dialect);
	}

	@Bean
	public QueryMappingConfiguration queryMappingConfiguration(
		JdbcMappingContext jdbcMappingContext,
		NamedParameterJdbcOperations operations,
		@Lazy RelationResolver relationResolver,
		JdbcCustomConversions conversions,
		Dialect dialect) {

		DefaultJdbcTypeFactory jdbcTypeFactory = new DefaultJdbcTypeFactory(operations.getJdbcOperations());
		JdbcConverter jdbcConverter = new AggregateResultJdbcConverter(
			jdbcMappingContext, relationResolver, conversions, jdbcTypeFactory, dialect.getIdentifierProcessing());

		return new EntityQueryMappingConfiguration(jdbcMappingContext, jdbcConverter);
	}

	@Bean
	public SqlParameterSourceFactory sqlParameterSourceFactory(
		JdbcMappingContext jdbcMappingContext, JdbcConverter jdbcConverter, Dialect dialect) {

		return new DefaultSqlParameterSourceFactory(jdbcMappingContext, jdbcConverter, dialect.getIdentifierProcessing());
	}

	@Bean
	public EntityJdbcProvider entityJdbcProvider(
		NamedParameterJdbcOperations jdbcOperations,
		SqlProvider sqlProvider,
		SqlParameterSourceFactory sqlParameterSourceFactory,
		QueryMappingConfiguration queryMappingConfiguration) {

		return new EntityJdbcProvider(
			jdbcOperations, sqlProvider, sqlParameterSourceFactory, queryMappingConfiguration);
	}
}
