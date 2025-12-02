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

package com.navercorp.spring.boot.starter.data.jdbc.plus.repository;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ObjectUtils;

/**
 * The type Jdbc plus repositories environment post processor.
 *
 * @author Myeonghyeon Lee
 */
public class JdbcPlusRepositoriesEnvironmentPostProcessor implements EnvironmentPostProcessor {
	private static final String ENABLED_JDBC_REPOSITORIES_PROPERTY = "spring.data.jdbc.repositories.enabled";
	private static final String ENABLED_JDBC_PLUS_REPOSITORIES_PROPERTY =
		"spring.data.jdbc.plus.repositories.enabled";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String enabledJdbcRepositories = environment.getProperty(ENABLED_JDBC_REPOSITORIES_PROPERTY);
		if (!ObjectUtils.isEmpty(enabledJdbcRepositories)) {
			return;
		}

		String enabledJdbcPlusRepositories = environment.getProperty(
			ENABLED_JDBC_PLUS_REPOSITORIES_PROPERTY);
		if (ObjectUtils.isEmpty(enabledJdbcPlusRepositories)
			|| Boolean.valueOf(enabledJdbcPlusRepositories) == Boolean.TRUE
		) {
			System.setProperty(ENABLED_JDBC_REPOSITORIES_PROPERTY, "false");
		}
	}
}
