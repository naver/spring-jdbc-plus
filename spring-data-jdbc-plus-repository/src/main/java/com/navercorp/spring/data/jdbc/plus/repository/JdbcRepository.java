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

package com.navercorp.spring.data.jdbc.plus.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * The interface Jdbc repository.
 *
 * @author Myeonghyeon Lee
 *
 * @param <T>  the entity type parameter
 * @param <ID> the id type parameter
 */
@NoRepositoryBean
public interface JdbcRepository<T, ID>
	extends PagingAndSortingRepository<T, ID>, CrudRepository<T, ID>, QueryByExampleExecutor<T> {

	/**
	 * Insert s.
	 *
	 * @param <S>    the type parameter
	 * @param entity the entity
	 * @return the s
	 */
	<S extends T> S insert(S entity);

	/**
	 * Insert all iterable.
	 *
	 * @param <S>      the type parameter
	 * @param entities the entities
	 * @return the iterable
	 */
	<S extends T> Iterable<S> insertAll(Iterable<S> entities);

	/**
	 * Update s.
	 *
	 * @param <S>    the type parameter
	 * @param entity the entity
	 * @return the s
	 */
	<S extends T> S update(S entity);

	/**
	 * Update all iterable.
	 *
	 * @param <S>      the type parameter
	 * @param entities the entities
	 * @return the iterable
	 */
	<S extends T> Iterable<S> updateAll(Iterable<S> entities);
}
