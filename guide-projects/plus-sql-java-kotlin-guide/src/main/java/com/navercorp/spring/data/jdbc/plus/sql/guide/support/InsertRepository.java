package com.navercorp.spring.data.jdbc.plus.sql.guide.support;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;

public interface InsertRepository<S> {
	S insert(S entity);

	class InsertRepositoryImpl<S> implements InsertRepository<S> {
		private final JdbcAggregateOperations operations;

		public InsertRepositoryImpl(JdbcAggregateOperations operations) {
			this.operations = operations;
		}

		@Override
		public S insert(S entity) {
			return operations.insert(entity);
		}
	}
}
