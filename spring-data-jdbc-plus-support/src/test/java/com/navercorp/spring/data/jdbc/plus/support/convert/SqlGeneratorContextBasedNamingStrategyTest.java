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

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.MappingJdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.mapping.Table;

import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn;
import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn.ValueType;

/**
 * Unit tests to verify a contextual {@link NamingStrategy} implementation that customizes using a user-centric
 * {@link ThreadLocal}. NOTE: Due to the need to verify SQL generation and {@link SqlGenerator}'s package-private status
 * suggests this unit test exist in this package, not {@literal org.springframework.data.jdbc.mappings.model}.
 *
 * @author Greg Turnquist
 * @author Myeonghyeon Lee
 */
public class SqlGeneratorContextBasedNamingStrategyTest {

	RelationalMappingContext context = new JdbcMappingContext();
	ThreadLocal<String> userHandler = new ThreadLocal<>();

	/**
	 * Use a {@link NamingStrategy}, but override the schema with a {@link ThreadLocal}-based setting.
	 */
	private final NamingStrategy contextualNamingStrategy = new NamingStrategy() {

		@Override
		public String getSchema() {
			return userHandler.get();
		}
	};

	@Test // DATAJDBC-107
	public void findOne() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.getFindOne();

			SoftAssertions softAssertions = new SoftAssertions();
			softAssertions.assertThat(sql) //
				.startsWith("SELECT") //
				.contains(user + ".dummy_entity.id AS id,") //
				.contains(user + ".dummy_entity.name AS name,") //
				.contains("ref.l1id AS ref_l1id") //
				.contains("ref.content AS ref_content") //
				.contains("FROM " + user + ".dummy_entity");
			softAssertions.assertAll();
		});
	}

	@Test // DATAJDBC-107
	public void cascadingDeleteFirstLevel() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createDeleteByPath(getPath("ref"));

			assertThat(sql).isEqualTo( //
				"DELETE FROM " //
					+ user + ".referenced_entity WHERE " //
					+ user + ".referenced_entity.dummy_entity = :id" //
			);
		});
	}

	@Test // DATAJDBC-107
	public void cascadingDeleteAllSecondLevel() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createDeleteByPath(getPath("ref.further"));

			assertThat(sql).isEqualTo( //
				"DELETE FROM " + user + ".second_level_referenced_entity " //
					+ "WHERE " + user + ".second_level_referenced_entity.referenced_entity IN " //
					+ "(SELECT " + user + ".referenced_entity.l1id "
					+ "FROM " + user + ".referenced_entity " //
					+ "WHERE " + user + ".referenced_entity.dummy_entity = :id)");
		});
	}

	@Test // DATAJDBC-107
	public void deleteAll() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createDeleteAllSql(null);

			assertThat(sql).isEqualTo("DELETE FROM " + user + ".dummy_entity");
		});
	}

	@Test // DATAJDBC-107
	public void cascadingDeleteAllFirstLevel() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createDeleteAllSql(getPath("ref"));

			assertThat(sql).isEqualTo( //
				"DELETE FROM " + user + ".referenced_entity "
					+ "WHERE " + user + ".referenced_entity.dummy_entity IS NOT NULL");
		});
	}

	@Test // DATAJDBC-107
	public void cascadingDeleteSecondLevel() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createDeleteAllSql(getPath("ref.further"));

			assertThat(sql).isEqualTo( //
				"DELETE FROM " + user + ".second_level_referenced_entity " //
					+ "WHERE " + user + ".second_level_referenced_entity.referenced_entity IN " //
					+ "(SELECT " + user + ".referenced_entity.l1id "
					+ "FROM " + user + ".referenced_entity " //
					+ "WHERE " + user + ".referenced_entity.dummy_entity IS NOT NULL)");
		});
	}

	@Test
	public void softDeleteAll() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSoftDeleteSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createSoftDeleteAllSql(null);

			assertThat(sql).isEqualTo("UPDATE " + user + ".article SET deleted = :deleted");
		});
	}

	@Test
	public void cascadingSoftDeleteAllFirstLevel() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSoftDeleteSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createSoftDeleteAllSql(getSoftDeletePath("ref"));

			assertThat(sql).isEqualTo(
				"UPDATE " + user + ".referenced_article "
					+ "SET reference_deleted = :reference_deleted "
					+ "WHERE " + user + ".referenced_article.article IS NOT NULL");
		});
	}

	@Test
	public void cascadingSoftDeleteSecondLevel() {

		testAgainstMultipleUsers(user -> {

			SqlGenerator sqlGenerator = configureSoftDeleteSqlGenerator(contextualNamingStrategy);

			String sql = sqlGenerator.createSoftDeleteAllSql(getSoftDeletePath("ref.further"));

			assertThat(sql).isEqualTo(
				"UPDATE " + user + ".second_referenced_article "
					+ "SET second_reference_deleted = :second_reference_deleted "
					+ "WHERE " + user + ".second_referenced_article.referenced_article IN ("
					+ "SELECT " + user + ".referenced_article.id "
					+ "FROM " + user + ".referenced_article "
					+ "WHERE " + user + ".referenced_article.article IS NOT NULL)");
		});
	}

	private PersistentPropertyPath<RelationalPersistentProperty> getPath(String path) {
		return PersistentPropertyPathTestUtils.getPath(this.context, path, DummyEntity.class);
	}

	private PersistentPropertyPath<RelationalPersistentProperty> getSoftDeletePath(String path) {
		return PersistentPropertyPathTestUtils.getPath(this.context, path, SoftDeleteArticle.class);
	}

	/**
	 * Take a set of user-based assertions and run them against multiple users, in different threads.
	 */
	private void testAgainstMultipleUsers(Consumer<String> testAssertions) {

		AtomicReference<Error> exception = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(2);

		threadedTest("User1", latch, testAssertions, exception);
		threadedTest("User2", latch, testAssertions, exception);

		try {
			if (!latch.await(10L, TimeUnit.SECONDS)) {
				fail("Test failed due to a time out.");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		Error ex = exception.get();
		if (ex != null) {
			throw ex;
		}
	}

	/**
	 * Inside a {@link Runnable}, fetch the {@link ThreadLocal}-based username
	 * and execute the provided set of assertions.
	 * Then signal through the provided {@link CountDownLatch}.
	 */
	private void threadedTest(String user, CountDownLatch latch, Consumer<String> testAssertions,
		AtomicReference<Error> exception) {

		new Thread(() -> {

			try {

				userHandler.set(user);
				testAssertions.accept(user);

			} catch (Error ex) {
				exception.compareAndSet(null, ex);
			} finally {
				latch.countDown();
			}

		}).start();
	}

	/**
	 * Plug in a custom {@link NamingStrategy} for this test case.
	 */
	private SqlGenerator configureSqlGenerator(NamingStrategy namingStrategy) {

		RelationalMappingContext context = new JdbcMappingContext(namingStrategy);
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		RelationalPersistentEntity<?> persistentEntity = context.getRequiredPersistentEntity(DummyEntity.class);

		return new SqlGenerator(context, converter, persistentEntity, NonQuotingDialect.INSTANCE);
	}

	private SqlGenerator configureSoftDeleteSqlGenerator(NamingStrategy namingStrategy) {

		RelationalMappingContext context = new JdbcMappingContext(namingStrategy);
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		RelationalPersistentEntity<?> persistentEntity = context.getRequiredPersistentEntity(SoftDeleteArticle.class);

		return new SqlGenerator(context, converter, persistentEntity, NonQuotingDialect.INSTANCE);
	}

	@SuppressWarnings("unused")
	static class DummyEntity {

		@Id
		Long id;
		String name;
		ReferencedEntity ref;
	}

	@SuppressWarnings("unused")
	static class ReferencedEntity {

		@Id
		Long l1id;
		String content;
		SecondLevelReferencedEntity further;
	}

	@SuppressWarnings("unused")
	static class SecondLevelReferencedEntity {

		@Id
		Long l2id;
		String something;
	}

	@Table("article")
	static class SoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn(type = ValueType.BOOLEAN, valueAsDeleted = "true")
		boolean deleted;

		@Version
		Integer version;

		ReferencedSoftDeleteArticle ref;
	}

	@Table("referenced_article")
	static class ReferencedSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn(type = ValueType.BOOLEAN, valueAsDeleted = "true")
		boolean reference_deleted; // @checkstyle:ignore

		SecondReferencedSoftDeleteArticle further;
	}

	@Table("second_referenced_article")
	static class SecondReferencedSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn(type = ValueType.BOOLEAN, valueAsDeleted = "true")
		boolean second_reference_deleted; // @checkstyle:ignore
	}
}
