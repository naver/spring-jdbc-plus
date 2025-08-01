/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.jdbc.plus.support.convert;

import java.util.Map;
import java.util.function.Function;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Provides {@link SqlGenerator}s per domain type. Instances get cached, so when asked multiple times for the same
 * domain type, the same generator will get returned.
 *
 * @author Jens Schauder
 * @author Mark Paluch
 * @author Milan Milanov
 * @author Myeonghyeon Lee
 *
 * COPY {@link org.springframework.data.jdbc.core.convert.SqlGeneratorSource}
 * VERIFIED: 91ddda0c3f97858e75cbf44ee23054559dd0daeb
 */
@SuppressWarnings("checkstyle:linelength")
public class SqlGeneratorSource {

	private final Map<Class<?>, SqlGenerator> cache = new ConcurrentReferenceHashMap<>();
	private final RelationalMappingContext context;
	private final JdbcConverter converter;
	private final Dialect dialect;

	// Generator for internal extension.
	Function<RelationalPersistentEntity<?>, SqlContexts> sqlContextsGenerator;

	public SqlGeneratorSource(RelationalMappingContext context, JdbcConverter converter, Dialect dialect) {
		this.context = context;
		this.converter = converter;
		this.dialect = dialect;
		this.sqlContextsGenerator = null;
	}

	/**
	 * DIFF
	 * Additional instantiate for internal extension.
	 */
	public SqlGenerator getSqlGenerator(Class<?> domainType) {
		return cache.computeIfAbsent(domainType, t -> {
			RelationalPersistentEntity<?> persistentEntity =
				context.getRequiredPersistentEntity(domainType);
			if (this.sqlContextsGenerator != null) {
				return new SqlGenerator(context, converter, persistentEntity, dialect,
					sqlContextsGenerator.apply(persistentEntity));
			} else {
				return new SqlGenerator(context, converter, persistentEntity, dialect);
			}
		});
	}

	/**
	 * @return the {@link Dialect} used by the created {@link SqlGenerator} instances. Guaranteed to be not
	 *         {@literal null}.
	 */
	public Dialect getDialect() {
		return dialect;
	}
}
