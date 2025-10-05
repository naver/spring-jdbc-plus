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

package com.navercorp.spring.boot.autoconfigure.data.jdbc.plus.sql;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.PlatformTransactionManager;

import reactor.core.publisher.Flux;

import com.navercorp.spring.data.jdbc.plus.sql.config.JdbcPlusSqlConfiguration;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.template.JdbcReactiveTemplate;
import com.navercorp.spring.data.jdbc.plus.support.convert.SqlProvider;

/**
 * The type Jdbc plus sql auto configuration.
 *
 * @author Myeonghyeon Lee
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({NamedParameterJdbcOperations.class, PlatformTransactionManager.class})
@ConditionalOnClass({NamedParameterJdbcOperations.class, AbstractJdbcConfiguration.class, EntityJdbcProvider.class})
@ConditionalOnProperty(
	prefix = "spring.data.jdbc.plus.sql",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true
)
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class JdbcPlusSqlAutoConfiguration {

	/**
	 * The type Spring boot jdbc configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(JdbcPlusSqlConfiguration.class)
	static class SpringBootJdbcConfiguration extends JdbcPlusSqlConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public SqlProvider sqlProvider(
			JdbcMappingContext jdbcMappingContext, JdbcConverter converter, Dialect dialect) {

			return super.sqlProvider(jdbcMappingContext, converter, dialect);
		}

		@Bean
		@ConditionalOnMissingBean
		public QueryMappingConfiguration queryMappingConfiguration(
			JdbcMappingContext mappingContext,
			NamedParameterJdbcOperations operations,
			@Lazy RelationResolver relationResolver,
			JdbcCustomConversions conversions,
			Dialect dialect
		) {
			return super.queryMappingConfiguration(
				mappingContext, operations, relationResolver, conversions, dialect);
		}

		@Bean
		@ConditionalOnMissingBean
		public SqlParameterSourceFactory sqlParameterSourceFactory(
			JdbcMappingContext mappingContext,
			JdbcConverter jdbcConverter,
			Dialect dialect
		) {
			return super.sqlParameterSourceFactory(mappingContext, jdbcConverter, dialect);
		}

		@Bean
		@ConditionalOnMissingBean
		public EntityJdbcProvider entityJdbcProvider(
			NamedParameterJdbcOperations jdbcOperations,
			SqlProvider sqlProvider,
			SqlParameterSourceFactory sqlParameterSourceFactory,
			QueryMappingConfiguration queryMappingConfiguration,
			ApplicationContext applicationContext
		) {
			return super.entityJdbcProvider(
				jdbcOperations,
				sqlProvider,
				sqlParameterSourceFactory,
				queryMappingConfiguration,
				applicationContext);
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnClass(Flux.class)
		public JdbcReactiveTemplate jdbcReactiveTemplate() {
			return new JdbcReactiveTemplate();
		}
	}
}
