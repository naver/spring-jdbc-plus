package com.navercorp.spring.boot.autoconfigure.data.jdbc.plus.sql;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
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

import com.navercorp.spring.data.jdbc.plus.sql.config.JdbcPlusSqlConfiguration;
import com.navercorp.spring.data.jdbc.plus.sql.convert.SqlProvider;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({NamedParameterJdbcOperations.class, PlatformTransactionManager.class})
@ConditionalOnClass({NamedParameterJdbcOperations.class, AbstractJdbcConfiguration.class, EntityJdbcProvider.class})
@ConditionalOnProperty(prefix = "spring.data.jdbc.plus.sql", name = "enabled", havingValue = "true",
	matchIfMissing = true)
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class JdbcPlusSqlAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(JdbcPlusSqlConfiguration.class)
	static class SpringBootJdbcConfiguration extends JdbcPlusSqlConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public SqlProvider sqlProvider(JdbcMappingContext jdbcMappingContext, JdbcConverter converter, Dialect dialect) {
			return super.sqlProvider(jdbcMappingContext, converter, dialect);
		}

		@Bean
		@ConditionalOnMissingBean
		public QueryMappingConfiguration queryMappingConfiguration(
			JdbcMappingContext mappingContext,
			NamedParameterJdbcOperations operations,
			@Lazy RelationResolver relationResolver,
			JdbcCustomConversions conversions,
			Dialect dialect) {

			return super.queryMappingConfiguration(mappingContext, operations, relationResolver, conversions, dialect);
		}

		@Bean
		@ConditionalOnMissingBean
		public SqlParameterSourceFactory sqlParameterSourceFactory(
			JdbcMappingContext mappingContext, JdbcConverter jdbcConverter, Dialect dialect) {

			return super.sqlParameterSourceFactory(mappingContext, jdbcConverter, dialect);
		}

		@Bean
		@ConditionalOnMissingBean
		public EntityJdbcProvider entityJdbcProvider(
			NamedParameterJdbcOperations jdbcOperations,
			SqlProvider sqlProvider,
			SqlParameterSourceFactory sqlParameterSourceFactory,
			QueryMappingConfiguration queryMappingConfiguration) {

			return super.entityJdbcProvider(
				jdbcOperations, sqlProvider, sqlParameterSourceFactory, queryMappingConfiguration);
		}
	}
}
