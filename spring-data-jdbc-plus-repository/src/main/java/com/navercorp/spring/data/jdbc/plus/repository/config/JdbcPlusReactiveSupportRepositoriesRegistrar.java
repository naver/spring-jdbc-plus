package com.navercorp.spring.data.jdbc.plus.repository.config;

import java.lang.annotation.Annotation;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * {@link org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesRegistrar}
 */
class JdbcPlusReactiveSupportRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableJdbcPlusReactiveSupportRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new JdbcPlusRepositoryReactiveSupportConfigExtension();
	}
}
