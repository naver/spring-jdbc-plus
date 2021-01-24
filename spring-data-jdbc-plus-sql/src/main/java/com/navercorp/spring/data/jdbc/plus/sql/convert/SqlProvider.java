/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.sql.convert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

/**
 * The type Sql provider.
 *
 * @author Myeonghyeon Lee
 */
public class SqlProvider {
	private final SqlGeneratorSource generatorSource;
	private final Map<Class<?>, String> selectColumnCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> fromTableCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> selectAggregateColumnCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> fromAggregateTableCache = new ConcurrentHashMap<>();

	/**
	 * Instantiates a new Sql provider.
	 *
	 * @param context   the context
	 * @param converter the converter
	 * @param dialect   the dialect
	 */
	public SqlProvider(RelationalMappingContext context, JdbcConverter converter, Dialect dialect) {
		this.generatorSource = new SqlGeneratorSource(context, converter, dialect);
	}

	/**
	 * Instantiates a new Sql provider.
	 *
	 * @param generatorSource the generator source
	 */
	public SqlProvider(SqlGeneratorSource generatorSource) {
		this.generatorSource = generatorSource;
	}

	/**
	 * Columns string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String columns(Class<?> entityType) {
		return this.selectColumnCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectFrom = generator.selectFrom();
			int fromIndex = selectFrom.lastIndexOf(" FROM ");
			if (fromIndex < 0) {
				return selectFrom.substring(6) + " ";
			}
			return selectFrom.substring(6, fromIndex) + " ";
		});
	}

	/**
	 * Tables string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String tables(Class<?> entityType) {
		return this.fromTableCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectFrom = generator.selectFrom();
			int fromIndex = selectFrom.lastIndexOf(" FROM ");
			if (fromIndex < 0) {
				throw new MappingException(
					"Can not generate tables clause. mapping id does not exist. entity: "
						+ entityType);
			}
			return " " + selectFrom.substring(fromIndex + 6) + " ";
		});
	}

	/**
	 * Aggregate columns string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String aggregateColumns(Class<?> entityType) {
		return this.selectAggregateColumnCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectAggregateFrom = generator.selectAggregateFrom();
			int fromIndex = selectAggregateFrom.lastIndexOf(" FROM ");
			return fromIndex > 0
				? selectAggregateFrom.substring(6, fromIndex) + " "
				: selectAggregateFrom + " ";
		});
	}

	/**
	 * Aggregate tables string.
	 *
	 * @param entityType the entity type
	 * @return the string
	 */
	public String aggregateTables(Class<?> entityType) {
		return this.fromAggregateTableCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectAggregateFrom = generator.selectAggregateFrom();
			int fromIndex = selectAggregateFrom.lastIndexOf(" FROM ");
			if (fromIndex < 0) {
				throw new MappingException(
					"Can not generate aggregateTables clause. mapping id does not exist. entity: "
						+ entityType);
			}
			return " " + selectAggregateFrom.substring(fromIndex + 6) + " ";
		});
	}

	/**
	 * Gets sql generator source.
	 *
	 * @return the sql generator source
	 */
	public SqlGeneratorSource getSqlGeneratorSource() {
		return this.generatorSource;
	}
}
