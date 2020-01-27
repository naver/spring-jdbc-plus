package com.navercorp.spring.data.jdbc.plus.repository.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.navercorp.spring.data.jdbc.plus.repository.support.JdbcPlusRepositoryFactoryBean;

/**
 * {@link org.springframework.data.jdbc.repository.config.EnableJdbcRepositories}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JdbcPlusReactiveSupportRepositoriesRegistrar.class)
public @interface EnableJdbcPlusReactiveSupportRepositories {
	String[] value() default {};

	String[] basePackages() default {};

	Class<?>[] basePackageClasses() default {};

	ComponentScan.Filter[] includeFilters() default {};

	ComponentScan.Filter[] excludeFilters() default {};

	boolean considerNestedRepositories() default false;

	Class<?> repositoryFactoryBeanClass() default JdbcPlusRepositoryFactoryBean.class;

	String namedQueriesLocation() default "";

	String repositoryImplementationPostfix() default "Impl";

	String jdbcOperationsRef() default "";

	String dataAccessStrategyRef() default "";
}
