package com.navercorp.spring.boot.autoconfigure.data.jdbc.plus.repository;

import java.lang.annotation.Annotation;

import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import com.navercorp.spring.data.jdbc.plus.repository.config.EnableJdbcPlusReactiveSupportRepositories;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryReactiveSupportConfigExtension;

class JdbcPlusReactiveSupportRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableJdbcPlusReactiveSupportRepositories.class;
	}

	@Override
	protected Class<?> getConfiguration() {
		return EnableJdbcPlusReactiveSupportRepositoriesConfiguration.class;
	}

	@Override
	protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
		return new JdbcPlusRepositoryReactiveSupportConfigExtension();
	}

	@EnableJdbcPlusReactiveSupportRepositories
	private static class EnableJdbcPlusReactiveSupportRepositoriesConfiguration {
	}
}
