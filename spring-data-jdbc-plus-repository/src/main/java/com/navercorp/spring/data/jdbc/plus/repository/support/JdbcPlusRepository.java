package com.navercorp.spring.data.jdbc.plus.repository.support;

import java.util.stream.Collectors;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.repository.support.SimpleJdbcRepository;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;

/**
 * {@link SimpleJdbcRepository}
 */
public class JdbcPlusRepository<T, ID> extends SimpleJdbcRepository<T, ID> implements JdbcRepository<T, ID> {
	private final JdbcAggregateOperations entityOperations;

	public JdbcPlusRepository(
		JdbcAggregateOperations entityOperations, PersistentEntity<T, ?> entity) {

		super(entityOperations, entity);
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
