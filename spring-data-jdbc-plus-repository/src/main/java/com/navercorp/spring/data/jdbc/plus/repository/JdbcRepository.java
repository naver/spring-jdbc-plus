package com.navercorp.spring.data.jdbc.plus.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface JdbcRepository<T, ID> extends PagingAndSortingRepository<T, ID> {

	<S extends T> S insert(S entity);

	<S extends T> Iterable<S> insertAll(Iterable<S> entities);

	<S extends T> S update(S entity);

	<S extends T> Iterable<S> updateAll(Iterable<S> entities);
}
