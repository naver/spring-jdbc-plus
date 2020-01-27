package com.navercorp.spring.boot.autoconfigure.data.jdbc.plus.repository;

import java.lang.annotation.Annotation;

import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import com.navercorp.spring.data.jdbc.plus.repository.config.EnableJdbcPlusRepositories;
import com.navercorp.spring.data.jdbc.plus.repository.config.JdbcPlusRepositoryConfigExtension;

class JdbcPlusRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableJdbcPlusRepositories.class;
	}

	@Override
	protected Class<?> getConfiguration() {
		return EnableJdbcPlusRepositoriesConfiguration.class;
	}

	@Override
	protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
		return new JdbcPlusRepositoryConfigExtension();
	}

	@EnableJdbcPlusRepositories
	private static class EnableJdbcPlusRepositoriesConfiguration {
	}
}
