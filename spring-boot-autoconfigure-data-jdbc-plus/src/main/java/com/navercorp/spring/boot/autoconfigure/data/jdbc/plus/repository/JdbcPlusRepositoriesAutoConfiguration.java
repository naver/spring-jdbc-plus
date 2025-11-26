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

package com.navercorp.spring.boot.autoconfigure.data.jdbc.plus.repository;

import java.util.Optional;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.relational.RelationalManagedTypes;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.PlatformTransactionManager;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;
import com.navercorp.spring.data.jdbc.plus.repository.config.AbstractJdbcPlusConfiguration;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryConfigExtension;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryReactiveSupportConfigExtension;

/**
 * The type Jdbc plus repositories auto configuration.
 *
 * @author Myeonghyeon Lee
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({NamedParameterJdbcOperations.class, PlatformTransactionManager.class})
@ConditionalOnClass({NamedParameterJdbcOperations.class, AbstractJdbcPlusConfiguration.class, JdbcRepository.class})
@ConditionalOnExpression(
	"!${spring.data.jdbc.repositories.enabled:true} " + " && ${spring.data.jdbc.plus.repositories.enabled:true}"
)
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class JdbcPlusRepositoriesAutoConfiguration {

	/**
	 * The type Jdbc plus repositories configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(
		prefix = "spring.data.jdbc.plus.repositories",
		name = "reactive-support",
		havingValue = "false",
		matchIfMissing = true
	)
	@ConditionalOnMissingBean(JdbcPlusRepositoryConfigExtension.class)
	@Import(JdbcPlusRepositoriesRegistrar.class)
	static class JdbcPlusRepositoriesConfiguration {
	}

	/**
	 * The type Jdbc plus reactive support repositories configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(
		prefix = "spring.data.jdbc.plus.repositories",
		name = "reactive-support",
		havingValue = "true"
	)
	@ConditionalOnMissingBean(JdbcPlusRepositoryReactiveSupportConfigExtension.class)
	@Import(JdbcPlusReactiveSupportRepositoriesRegistrar.class)
	static class JdbcPlusReactiveSupportRepositoriesConfiguration {
	}

	/**
	 * The type Spring boot jdbc configuration.
	 */
	@Configuration
	@ConditionalOnMissingBean(AbstractJdbcPlusConfiguration.class)
	static class SpringBootJdbcConfiguration extends AbstractJdbcPlusConfiguration {
		@Override
		@Bean
		@ConditionalOnMissingBean
		public RelationalManagedTypes jdbcManagedTypes() throws ClassNotFoundException {
			return super.jdbcManagedTypes();
		}

		@Override
		@Bean
		@ConditionalOnMissingBean
		public JdbcMappingContext jdbcMappingContext(
			Optional<NamingStrategy> namingStrategy,
			JdbcCustomConversions customConversions,
			RelationalManagedTypes jdbcManagedTypes
		) {
			return super.jdbcMappingContext(namingStrategy, customConversions, jdbcManagedTypes);
		}

		@Override
		@Bean
		@ConditionalOnMissingBean
		public JdbcConverter jdbcConverter(
			JdbcMappingContext mappingContext,
			NamedParameterJdbcOperations operations,
			@Lazy RelationResolver relationResolver,
			JdbcCustomConversions conversions,
			JdbcDialect dialect
		) {
			return super.jdbcConverter(mappingContext, operations, relationResolver, conversions, dialect);
		}

		@Override
		@Bean
		@ConditionalOnMissingBean
		public JdbcCustomConversions jdbcCustomConversions() {
			return super.jdbcCustomConversions();
		}

		@Override
		@Bean
		@ConditionalOnMissingBean
		public JdbcAggregateTemplate jdbcAggregateTemplate(
			ApplicationContext applicationContext,
			JdbcMappingContext mappingContext,
			JdbcConverter converter,
			DataAccessStrategy dataAccessStrategy
		) {
			return super.jdbcAggregateTemplate(applicationContext, mappingContext, converter, dataAccessStrategy);
		}

		@Override
		@Bean
		@ConditionalOnMissingBean
		public DataAccessStrategy dataAccessStrategyBean(
			NamedParameterJdbcOperations operations,
			JdbcConverter jdbcConverter, JdbcMappingContext context,
			JdbcDialect dialect
		) {
			return super.dataAccessStrategyBean(operations, jdbcConverter, context, dialect);
		}

		@Override
		@Bean
		@ConditionalOnMissingBean
		public JdbcDialect jdbcDialect(NamedParameterJdbcOperations operations) {
			return super.jdbcDialect(operations);
		}
	}
}
