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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.PlatformTransactionManager;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryConfigExtension;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryReactiveSupportConfigExtension;

/**
 * The type Jdbc plus repositories auto configuration.
 *
 * @author Myeonghyeon Lee
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({NamedParameterJdbcOperations.class, PlatformTransactionManager.class})
@ConditionalOnClass({NamedParameterJdbcOperations.class, AbstractJdbcConfiguration.class, JdbcRepository.class})
@ConditionalOnExpression("!${spring.data.jdbc.repositories.enabled:true} "
	+ " && ${spring.data.jdbc.plus.repositories.enabled:true}")
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class JdbcPlusRepositoriesAutoConfiguration {

	/**
	 * The type Jdbc plus repositories configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.data.jdbc.plus.repositories", name = "reactive-support",
		havingValue = "false", matchIfMissing = true)
	@ConditionalOnMissingBean(JdbcPlusRepositoryConfigExtension.class)
	@Import(JdbcPlusRepositoriesRegistrar.class)
	static class JdbcPlusRepositoriesConfiguration {
	}

	/**
	 * The type Jdbc plus reactive support repositories configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.data.jdbc.plus.repositories", name = "reactive-support",
		havingValue = "true")
	@ConditionalOnMissingBean(JdbcPlusRepositoryReactiveSupportConfigExtension.class)
	@Import(JdbcPlusReactiveSupportRepositoriesRegistrar.class)
	static class JdbcPlusReactiveSupportRepositoriesConfiguration {
	}

	/**
	 * The type Spring boot jdbc configuration.
	 */
	@Configuration
	@ConditionalOnMissingBean(AbstractJdbcConfiguration.class)
	static class SpringBootJdbcConfiguration extends AbstractJdbcConfiguration {
	}
}
