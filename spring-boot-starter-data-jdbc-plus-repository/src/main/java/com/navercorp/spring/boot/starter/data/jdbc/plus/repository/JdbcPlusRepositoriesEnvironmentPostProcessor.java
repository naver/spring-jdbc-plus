package com.navercorp.spring.boot.starter.data.jdbc.plus.repository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

public class JdbcPlusRepositoriesEnvironmentPostProcessor implements EnvironmentPostProcessor {
	private static final String ENABLED_JDBC_REPOSITORIES_PROPERTY = "spring.data.jdbc.repositories.enabled";
	private static final String ENABLED_JDBC_PLUS_REPOSITORIES_PROPERTY =
		"spring.data.jdbc.plus.repositories.enabled";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String enabledJdbcRepositories = environment.getProperty(ENABLED_JDBC_REPOSITORIES_PROPERTY);
		if (!StringUtils.isEmpty(enabledJdbcRepositories)) {
			return;
		}

		String enabledJdbcPlusRepositories = environment.getProperty(
			ENABLED_JDBC_PLUS_REPOSITORIES_PROPERTY);
		if (StringUtils.isEmpty(enabledJdbcPlusRepositories)
			|| Boolean.valueOf(enabledJdbcPlusRepositories) == Boolean.TRUE) {

			System.setProperty(ENABLED_JDBC_REPOSITORIES_PROPERTY, "false");
		}
	}
}
