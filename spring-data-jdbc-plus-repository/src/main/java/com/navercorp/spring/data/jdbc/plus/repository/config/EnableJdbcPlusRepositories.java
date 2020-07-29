/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.repository.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactoryBean;

import com.navercorp.spring.data.jdbc.plus.repository.support.JdbcPlusRepository;
import com.navercorp.spring.data.jdbc.plus.repository.support.JdbcPlusRepositoryFactoryBean;

/**
 * Annotation to enable JDBC Plus repositories.
 * Will scan the package of the annotated configuration class for Spring Data
 * repositories by default.
 *
 * @author Myeonghyeon Lee
 *
 * {@link org.springframework.data.jdbc.repository.config.EnableJdbcRepositories}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JdbcPlusRepositoriesRegistrar.class)
public @interface EnableJdbcPlusRepositories {
	/**
	 * Alias for the {@link #basePackages()} attribute.
	 * Allows for more concise annotation declarations e.g.:
	 * {@code @EnableJdbcPlusRepositories("org.my.pkg")} instead of
	 * {@code @EnableJdbcPlusRepositories(basePackages="org.my.pkg")}.
	 *
	 * @return value
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components.
	 * {@link #value()} is an alias for (and mutually exclusive with) this attribute.
	 * Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
	 *
	 * @return basePackages
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to
	 * scan for annotated components. The package of each class specified will be scanned.
	 * Consider creating a special no-op marker class or interface in
	 * each package that serves no purpose other than being referenced by this attribute.
	 *
	 * @return basePackageClasses
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Specifies which types are eligible for component scanning.
	 * Further narrows the set of candidate components from everything in {@link #basePackages()} to
	 * everything in the base packages that matches the given filter or filters.
	 *
	 * @return includeFilters
	 */
	ComponentScan.Filter[] includeFilters() default {};

	/**
	 * Specifies which types are not eligible for component scanning.
	 *
	 * @return excludeFilters
	 */
	ComponentScan.Filter[] excludeFilters() default {};

	/**
	 * Returns the postfix to be used when looking up custom repository implementations.
	 * Defaults to {@literal Impl}. So for a repository named {@code PersonRepository}
	 * the corresponding implementation class will be looked up scanning
	 * for {@code PersonRepositoryImpl}.
	 *
	 * @return repositoryImplementationPostfix
	 */
	String repositoryImplementationPostfix() default "Impl";

	/**
	 * Configures the location of where to find the Spring Data named queries properties file.
	 * Will default to {@code META-INF/jdbc-named-queries.properties}.
	 *
	 * @return namedQueriesLocation
	 */
	String namedQueriesLocation() default "";

	/**
	 * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
	 * {@link JdbcRepositoryFactoryBean}.
	 *
	 * @return repositoryFactoryBeanClass
	 */
	Class<?> repositoryFactoryBeanClass() default JdbcPlusRepositoryFactoryBean.class;

	/**
	 * Repository base class class.
	 *
	 * @return the class
	 */
	Class<?> repositoryBaseClass() default JdbcPlusRepository.class;

	/**
	 * Configures whether nested repository-interfaces (e.g. defined as inner classes)
	 * should be discovered by the repositories infrastructure.
	 *
	 * @return considerNestedRepositories
	 */
	boolean considerNestedRepositories() default false;

	/**
	 * Configures the name of the
	 * {@link org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations} bean
	 * definition to be used to create repositories discovered through this annotation.
	 * Defaults to {@code namedParameterJdbcTemplate}.
	 *
	 * @return jdbcOperationsRef
	 */
	String jdbcOperationsRef() default "";

	/**
	 * Configures the name of the
	 * {@link org.springframework.data.jdbc.core.convert.DataAccessStrategy} bean definition to
	 * be used to create repositories discovered through this annotation.
	 * Defaults to {@code defaultDataAccessStrategy}.
	 *
	 * @return dataAccessStrategyRef
	 */
	String dataAccessStrategyRef() default "";
}
