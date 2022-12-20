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

package com.navercorp.spring.data.jdbc.plus.repository.support;

import java.util.stream.Collectors;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.support.SimpleJdbcRepository;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;

/**
 * Default implementation of the {@link com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository} interface.
 * <p>
 *
 * @author Myeonghyeon Lee
 *
 * {@link SimpleJdbcRepository}
 *
 * @param <T>  the entity type parameter
 * @param <ID> the id type parameter
 */
public class JdbcPlusRepository<T, ID> extends SimpleJdbcRepository<T, ID> implements JdbcRepository<T, ID> {
	private final JdbcAggregateOperations entityOperations;

	/**
	 * Instantiates a new Jdbc plus repository.
	 *
	 * @param entityOperations the entity operations
	 * @param entity           the entity
	 */
	public JdbcPlusRepository(
		JdbcAggregateOperations entityOperations, PersistentEntity<T, ?> entity, JdbcConverter converter) {

		super(entityOperations, entity, converter);
		this.entityOperations = entityOperations;
	}

	@Transactional
	@Override
	public <S extends T> S insert(S instance) {
		return entityOperations.insert(instance);
	}

	@Transactional
	@Override
	public <S extends T> Iterable<S> insertAll(Iterable<S> entities) {
		return Streamable.of(entities).stream()
			.map(this::insert)
			.collect(Collectors.toList());
	}

	@Transactional
	@Override
	public <S extends T> S update(S instance) {
		return entityOperations.update(instance);
	}

	@Transactional
	@Override
	public <S extends T> Iterable<S> updateAll(Iterable<S> entities) {
		return Streamable.of(entities).stream()
			.map(this::update)
			.collect(Collectors.toList());
	}
}
