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

package com.navercorp.spring.data.jdbc.plus.sql.support.template;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * The type Jdbc reactive template.
 *
 * @author Myeonghyeon Lee
 * @author IAM20
 */
public class JdbcReactiveTemplate {
	private final Log logger = LogFactory.getLog(this.getClass());

	private final Scheduler scheduler;
	private final int defaultQueueSize;
	private final long defaultBufferTimeout;

	/**
	 * Instantiates a new Jdbc reactive template.
	 */
	public JdbcReactiveTemplate() {
		this(Schedulers.boundedElastic(), 100, 10_000);
	}

	/**
	 * Instantiates a new Jdbc reactive template.
	 *
	 * @param scheduler            the scheduler
	 * @param defaultQueueSize     the default queue size
	 * @param defaultBufferTimeout the default buffer timeout in milliseconds
	 */
	public JdbcReactiveTemplate(
		Scheduler scheduler,
		int defaultQueueSize,
		long defaultBufferTimeout
	) {
		this.scheduler = scheduler;
		this.defaultQueueSize = defaultQueueSize;
		this.defaultBufferTimeout = defaultBufferTimeout;
	}

	@SuppressWarnings("unchecked")
	private static <R> FluxItem<R> endItem() {
		return FluxItem.END_ITEM;
	}

	@SuppressWarnings("unchecked")
	private static <R> FluxItem<R> errorItem(Exception e) {
		return FluxItem.errorInstance(e);
	}

	/**
	 * Query flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @return the flux
	 */
	public <R> Flux<R> queryFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper) {

		return this.queryFlux(
			sql,
			jdbcOperations,
			params,
			rowMapper,
			this.scheduler,
			this.defaultQueueSize,
			this.defaultBufferTimeout);
	}

	/**
	 * Query flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @param scheduler      the scheduler
	 * @return the flux
	 */
	public <R> Flux<R> queryFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper,
		Scheduler scheduler
	) {
		return this.queryFlux(
			sql,
			jdbcOperations,
			params,
			rowMapper,
			scheduler,
			this.defaultQueueSize,
			this.defaultBufferTimeout);
	}

	/**
	 * Query flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @param queueSize      the queue size
	 * @param bufferTimeout  the buffer timeout
	 * @return the flux
	 */
	public <R> Flux<R> queryFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper,
		int queueSize,
		long bufferTimeout
	) {
		return this.queryFlux(
			sql, jdbcOperations, params, rowMapper, this.scheduler, queueSize, bufferTimeout);
	}

	/**
	 * Query flux flux.
	 *
	 * @param <R>            the type parameter
	 * @param sql            the sql
	 * @param jdbcOperations the jdbc operations
	 * @param params         the params
	 * @param rowMapper      the row mapper
	 * @param scheduler      the scheduler
	 * @param queueSize      the queue size
	 * @param bufferTimeout  the buffer timeout
	 * @return the flux
	 */
	public <R> Flux<R> queryFlux(
		String sql,
		NamedParameterJdbcOperations jdbcOperations,
		SqlParameterSource params,
		RowMapper<R> rowMapper,
		Scheduler scheduler,
		int queueSize,
		long bufferTimeout
	) {
		BlockingQueue<FluxItem<R>> queue = new LinkedBlockingQueue<>(queueSize);
		AtomicBoolean isClosed = new AtomicBoolean(false);

		return generateFluxFromQueue(queue, bufferTimeout, isClosed)
			.doOnCancel(() -> isClosed.set(true))
			.doFirst(() -> scheduler.schedule(() -> {
				try {
					jdbcOperations.query(sql, params, new RowCountCallbackHandler() {
						@Override
						public void processRow(ResultSet resultSet, int rowNum) throws SQLException {
							if (isClosed.get()) {
								throw new DataAccessResourceFailureException("Connection closed by client.");
							}

							FluxItem<R> item = new FluxItem<>(rowMapper.mapRow(resultSet, rowNum));
							insertToBlockingQueue(queue, item, isClosed, bufferTimeout);
						}
					});
				} catch (Exception e) {
					isClosed.set(true);
					insertToBlockingQueue(queue, errorItem(e), isClosed, bufferTimeout);
					logger.error("Failed to generate flux.", e);
					throw e; /* To propagate exception to subscriber */
				}

				if (!isClosed.get()) {
					insertToBlockingQueue(queue, endItem(), isClosed, bufferTimeout);
				}
			}));
	}

	/**
	 * @param e the exception
	 * @throws Exception will be propagated to flux.
	 */
	protected void handleError(Exception e) throws Exception {
		if (e == null) {
			return;
		}
		logger.error("Exception occured while reading flux", e);
	}

	private <R> void insertToBlockingQueue(
		BlockingQueue<FluxItem<R>> queue,
		FluxItem<R> item,
		AtomicBoolean isClosed,
		long bufferTimeout
	) {
		try {
			if (!queue.offer(item, bufferTimeout, TimeUnit.MILLISECONDS)) {
				/* Close the flux. */
				isClosed.set(true);
				throw new TimeoutException("Cannot insert into blocking queue.");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("InterruptedException occurred", e);
		} catch (TimeoutException e) {
			throw new DataAccessResourceFailureException("Timeout to get item from queue.", e);
		}
	}

	private <R> Flux<R> generateFluxFromQueue(
		BlockingQueue<FluxItem<R>> queue,
		long bufferTimeout,
		AtomicBoolean isClosed
	) {
		return Flux.generate(sink -> {
			if (isClosed.get()) {
				/* Flux is closed by db. */
				sink.error(new DataAccessResourceFailureException(
					"Database Connection is closed."));
			}
			try {
				FluxItem<R> row = queue.poll(bufferTimeout, TimeUnit.MILLISECONDS);
				if (row == null) {
					sink.error(new DataAccessResourceFailureException(
						"Cannot take element from blocking queue."));
					return;
				}
				if (row.isError()) {
					try {
						handleError(row.getError());
					} catch (Exception e) {
						sink.error(e);
					}
				}
				if (row.isEnd()) {
					sink.complete();
					return;
				}
				sink.next(row.getItem());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("InterruptedException occurred", e);
			}
		});
	}

	private static class FluxItem<R> {
		@SuppressWarnings("rawtypes")
		private static final FluxItem END_ITEM = new EndOfFluxItem();

		private final R item;

		/**
		 * Instantiates a new Flux item.
		 *
		 * @param item the item
		 */
		private FluxItem(R item) {
			this.item = item;
		}

		/**
		 * Is end boolean.
		 *
		 * @return the boolean
		 */
		protected boolean isEnd() {
			return false;
		}

		/**
		 * Is error boolean.
		 *
		 * @return the boolean
		 */
		protected boolean isError() {
			return false;
		}

		/**
		 * Gets item.
		 *
		 * @return the item
		 */
		private R getItem() {
			return this.item;
		}

		/**
		 * Gets error.
		 *
		 * @return the error
		 */
		@Nullable
		protected Exception getError() {
			return null;
		}

		@SuppressWarnings("rawtypes")
		private static FluxItem errorInstance(Exception e) {
			return new ErrorFluxItem(e);
		}
	}

	private static class EndOfFluxItem<R> extends FluxItem<R> {
		private EndOfFluxItem() {
			super(null);
		}

		@Override
		protected boolean isEnd() {
			return true;
		}
	}

	private static class ErrorFluxItem<R> extends FluxItem<R> {
		Exception error;

		private ErrorFluxItem(Exception error) {
			super(null);
			this.error = error;
		}

		@Override
		protected boolean isError() {
			return true;
		}

		@Override
		protected boolean isEnd() {
			return true;
		}

		@Override
		public Exception getError() {
			return this.error;
		}
	}
}
