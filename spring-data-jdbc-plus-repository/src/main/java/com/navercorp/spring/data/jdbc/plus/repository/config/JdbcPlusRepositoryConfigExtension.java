package com.navercorp.spring.data.jdbc.plus.repository.config;

import org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension;

import com.navercorp.spring.data.jdbc.plus.repository.support.JdbcPlusRepositoryFactoryBean;

/**
 * {@link org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension}
 */
public class JdbcPlusRepositoryConfigExtension extends JdbcRepositoryConfigExtension {
	@Override
	public String getModuleName() {
		return "JDBC-PLUS-REPOSITORY";
	}

	@Override
	public String getRepositoryFactoryBeanClassName() {
		return JdbcPlusRepositoryFactoryBean.class.getName();
	}
}
