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

import java.sql.ResultSet;
import java.util.List;

import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * The type Aggregate result set extractor.
 *
 * @author Myeonghyeon Lee
 *
 * @param <T> the type parameter
 */
public class AggregateResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
	private final RelationalPersistentEntity<T> entity;
	private final AggregateResultJdbcConverter jdbcConverter;

	/**
	 * Instantiates a new Aggregate result set extractor.
	 *
	 * @param type          the type
	 * @param jdbcConverter the jdbc converter
	 */
	@SuppressWarnings("unchecked")
	public AggregateResultSetExtractor(Class<T> type, AggregateResultJdbcConverter jdbcConverter) {
		this.entity = (RelationalPersistentEntity<T>)jdbcConverter.getMappingContext()
			.getRequiredPersistentEntity(type);
		this.jdbcConverter = jdbcConverter;
	}

	/**
	 * Instantiates a new Aggregate result set extractor.
	 *
	 * @param entity        the entity
	 * @param jdbcConverter the jdbc converter
	 */
	public AggregateResultSetExtractor(
		RelationalPersistentEntity<T> entity,
		AggregateResultJdbcConverter jdbcConverter
	) {
		this.entity = entity;
		this.jdbcConverter = jdbcConverter;
	}

	@Override
	public List<T> extractData(ResultSet resultSet) {
		return this.jdbcConverter.mapAggregate(entity, resultSet);
	}
}
