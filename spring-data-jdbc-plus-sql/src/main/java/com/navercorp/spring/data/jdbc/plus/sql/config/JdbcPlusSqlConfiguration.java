/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.sql.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.convert.DefaultJdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.navercorp.spring.data.jdbc.plus.sql.convert.AggregateResultJdbcConverter;
import com.navercorp.spring.data.jdbc.plus.sql.convert.SqlProvider;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.DefaultSqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityQueryMappingConfiguration;

/**
 * The type Jdbc plus sql configuration.
 *
 * @author Myeonghyeon Lee
 */
@Configuration
public class JdbcPlusSqlConfiguration {
	/**
	 * Sql provider sql provider.
	 *
	 * @param jdbcMappingContext the jdbc mapping context
	 * @param converter the converter
	 * @param dialect the dialect
	 * @return the sql provider
	 */
	@Bean
	public SqlProvider sqlProvider(
		JdbcMappingContext jdbcMappingContext, JdbcConverter converter, Dialect dialect) {

		return new SqlProvider(jdbcMappingContext, converter, dialect);
	}

	/**
	 * Query mapping configuration query mapping configuration.
	 *
	 * @param jdbcMappingContext the jdbc mapping context
	 * @param operations the operations
	 * @param relationResolver the relation resolver
	 * @param conversions the conversions
	 * @param dialect the dialect
	 * @return the query mapping configuration
	 */
	@Bean
	public QueryMappingConfiguration queryMappingConfiguration(
		JdbcMappingContext jdbcMappingContext,
		NamedParameterJdbcOperations operations,
		@Lazy RelationResolver relationResolver,
		JdbcCustomConversions conversions,
		Dialect dialect) {

		DefaultJdbcTypeFactory jdbcTypeFactory = new DefaultJdbcTypeFactory(
			operations.getJdbcOperations());
		JdbcConverter jdbcConverter = new AggregateResultJdbcConverter(
			jdbcMappingContext,
			relationResolver,
			conversions,
			jdbcTypeFactory,
			dialect.getIdentifierProcessing());

		return new EntityQueryMappingConfiguration(jdbcMappingContext, jdbcConverter);
	}

	/**
	 * Sql parameter source factory sql parameter source factory.
	 *
	 * @param jdbcMappingContext the jdbc mapping context
	 * @param jdbcConverter the jdbc converter
	 * @param dialect the dialect
	 * @return the sql parameter source factory
	 */
	@Bean
	public SqlParameterSourceFactory sqlParameterSourceFactory(
		JdbcMappingContext jdbcMappingContext, JdbcConverter jdbcConverter, Dialect dialect) {

		return new DefaultSqlParameterSourceFactory(
			jdbcMappingContext, jdbcConverter, dialect.getIdentifierProcessing());
	}

	/**
	 * Entity jdbc provider entity jdbc provider.
	 *
	 * @param jdbcOperations the jdbc operations
	 * @param sqlProvider the sql provider
	 * @param sqlParameterSourceFactory the sql parameter source factory
	 * @param queryMappingConfiguration the query mapping configuration
	 * @param applicationContext the application context
	 * @return the entity jdbc provider
	 */
	@Bean
	public EntityJdbcProvider entityJdbcProvider(
		NamedParameterJdbcOperations jdbcOperations,
		SqlProvider sqlProvider,
		SqlParameterSourceFactory sqlParameterSourceFactory,
		QueryMappingConfiguration queryMappingConfiguration,
		ApplicationContext applicationContext) {

		return new EntityJdbcProvider(
			jdbcOperations,
			sqlProvider,
			sqlParameterSourceFactory,
			queryMappingConfiguration,
			applicationContext,
			EntityCallbacks.create(applicationContext));
	}
}
