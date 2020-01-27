package com.navercorp.spring.data.jdbc.plus.repository.config;

import org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension;
import org.springframework.data.repository.core.RepositoryMetadata;

import com.navercorp.spring.data.jdbc.plus.repository.support.JdbcPlusRepositoryFactoryBean;

/**
 * {@link org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension}
 */
public class JdbcPlusRepositoryReactiveSupportConfigExtension extends JdbcRepositoryConfigExtension {
	@Override
	public String getModuleName() {
		return "JDBC-PLUS-REPOSITORY-REACTIVE-SUPPORT";
	}

	@Override
	public String getRepositoryFactoryBeanClassName() {
		return JdbcPlusRepositoryFactoryBean.class.getName();
	}

	@Override
	protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
		return true;    // true for support reactive repository custom
	}
}
