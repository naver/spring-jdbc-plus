package com.navercorp.spring.boot.autoconfigure.data.jdbc.plus.repository;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.PlatformTransactionManager;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryConfigExtension;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryReactiveSupportConfigExtension;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({NamedParameterJdbcOperations.class, PlatformTransactionManager.class})
@ConditionalOnClass({NamedParameterJdbcOperations.class, AbstractJdbcConfiguration.class, JdbcRepository.class})
@ConditionalOnExpression("!${spring.data.jdbc.repositories.enabled:true} "
	+ " && ${spring.data.jdbc.plus.repositories.enabled:true}")
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class JdbcPlusRepositoriesAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.data.jdbc.plus.repositories", name = "reactive-support",
		havingValue = "false", matchIfMissing = true)
	@ConditionalOnMissingBean(JdbcPlusRepositoryConfigExtension.class)
	@Import(JdbcPlusRepositoriesRegistrar.class)
	static class JdbcPlusRepositoriesConfiguration {
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.data.jdbc.plus.repositories", name = "reactive-support",
		havingValue = "true")
	@ConditionalOnMissingBean(JdbcPlusRepositoryReactiveSupportConfigExtension.class)
	@Import(JdbcPlusReactiveSupportRepositoriesRegistrar.class)
	static class JdbcPlusReactiveSupportRepositoriesConfiguration {
	}

	@Configuration
	@ConditionalOnMissingBean({AbstractJdbcConfiguration.class, JdbcConfiguration.class})
	static class SpringBootJdbcConfiguration extends AbstractJdbcConfiguration {
	}
}
