package com.navercorp.spring.data.jdbc.plus.sql.guide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

import com.navercorp.spring.data.jdbc.plus.sql.parametersource.EntityConvertibleSqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.jdbc.plus.support.parametersource.ConvertibleParameterSourceFactory;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.NoneFallbackParameterSource;

@Configuration
public class JdbcConfiguration {

	@Bean
	@Primary
	public SqlParameterSourceFactory sqlParameterSourceFactory(
		RelationalMappingContext mappingContext,
		JdbcConverter jdbcConverter) {
		return new EntityConvertibleSqlParameterSourceFactory(
			new ConvertibleParameterSourceFactory(
				new DefaultJdbcParameterSourceConverter(),
				new NoneFallbackParameterSource()
			),
			mappingContext,
			jdbcConverter,
			IdentifierProcessing.ANSI
		);
	}
}
