/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2025 NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.support.convert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.MappingJdbcConverter;
import org.springframework.data.jdbc.core.dialect.JdbcMySqlDialect;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

import com.navercorp.spring.jdbc.plus.commons.annotations.SqlFunction;
import com.navercorp.spring.jdbc.plus.commons.annotations.SqlTableAlias;

/**
 * @author Myeonghyeon Lee
 */
class SqlProviderTest {
	@Test
	@DisplayName("테이블 기본 컬럼 생성 규칙")
	void columns() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String columns = sut.columns(TestInnerEntity.class);

		// then
		assertThat(columns).contains(
			"test_inner_entity.cty AS cty, "
				+ "test_inner_entity.state AS state"
		);
	}

	@Test
	@DisplayName("Embedded 와 Alias Table column 을 찾아가서 변환한다.")
	void columnsWithEmbeddedAndAlias() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String columns = sut.columns(TestOuterEntity.class);

		// then
		assertThat(columns).contains(
			"ts.tester_id AS tester_id, "
				+ "address.tester_outer_id AS address_tester_outer_id, "
				+ "ts.tester_nm AS tester_nm, "
				+ "address.cty AS address_cty, "
				+ "address.state AS address_state, "
				+ "ts.adr_cty AS adr_cty, "
				+ "ts.adr_state AS adr_state"
		);
	}

	@Test
	@DisplayName("Root 에 @SqlTableAlias 이 없으면 Alias 가 없다.")
	void columnsWithNoRootTableAlias() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String columns = sut.columns(TestEntityNoRootTableAlias.class);

		// then
		assertThat(columns).contains(
			"test_entity_no_root_table_alias.root_id AS root_id, "
				+ "test_entity_no_root_table_alias.root_name AS root_name, "
				+ "tOuter.test_root_id AS tOuter_test_root_id, "
				+ "tOuter.tester_id AS tOuter_tester_id, "
				+ "tOuter_address.tester_outer_id AS tOuter_address_tester_outer_id, "
				+ "tOuter.tester_nm AS tOuter_tester_nm, "
				+ "test_entity_no_root_table_alias.outer_tester_id AS outer_tester_id, "
				+ "outer_address.tester_outer_id AS outer_address_tester_outer_id, "
				+ "test_entity_no_root_table_alias.outer_tester_nm AS outer_tester_nm, "
				+ "tOuter_address.cty AS tOuter_address_cty, "
				+ "tOuter_address.state AS tOuter_address_state, "
				+ "tOuter.adr_cty AS tOuter_adr_cty, "
				+ "tOuter.adr_state AS tOuter_adr_state, "
				+ "outer_address.cty AS outer_address_cty, "
				+ "outer_address.state AS outer_address_state, "
				+ "test_entity_no_root_table_alias.outer_adr_cty AS outer_adr_cty, "
				+ "test_entity_no_root_table_alias.outer_adr_state AS outer_adr_state"
		);
	}

	@Test
	@DisplayName("Nested 복합 클래스 컬럼 추출")
	void columnsEmbeddedNestedTableAlias() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String columns = sut.columns(TestEmbeddedNested.class);

		// then
		assertThat(columns).contains(
			"teie.root_id AS root_id, "
				+ "teie.root_name AS root_name, "
				+ "testOuter.test_root_id AS testOuter_test_root_id, "
				+ "COALESCE(teie.age, 0) AS age, "
				+ "teie.tester_id AS tester_id, "
				+ "teie.tester_nm AS tester_nm, "
				+ "testOuter.tester_id AS testOuter_tester_id, "
				+ "testOuter_address.tester_outer_id AS testOuter_address_tester_outer_id, "
				+ "testOuter.tester_nm AS testOuter_tester_nm, "
				+ "teie.outer_tester_id AS outer_tester_id, "
				+ "outer_address.tester_outer_id AS outer_address_tester_outer_id, "
				+ "teie.outer_tester_nm AS outer_tester_nm, "
				+ "testOuter_address.cty AS testOuter_address_cty, "
				+ "testOuter_address.state AS testOuter_address_state, "
				+ "testOuter.adr_cty AS testOuter_adr_cty, "
				+ "testOuter.adr_state AS testOuter_adr_state, "
				+ "outer_address.cty AS outer_address_cty, "
				+ "outer_address.state AS outer_address_state, "
				+ "teie.outer_adr_cty AS outer_adr_cty, "
				+ "teie.outer_adr_state AS outer_adr_state"
		);
	}

	@Test
	@DisplayName("Nested 복합 클래스 테이블 추출")
	void tablesEmbeddedNestedTableAlias() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String tables = sut.tables(TestEmbeddedNested.class);

		// then
		assertThat(tables).contains(
			"teie LEFT OUTER JOIN test_outer_entity testOuter "
				+ "ON testOuter.test_root_id = teie.root_id "
				+ "LEFT OUTER JOIN test_inner_entity testOuter_address "
				+ "ON testOuter_address.tester_outer_id = teie.root_id "
				+ "LEFT OUTER JOIN test_inner_entity outer_address "
				+ "ON outer_address.tester_outer_id = teie.root_id"
		);
	}

	@Test
	@DisplayName("@SqlFunction 은 Column 변환식을 사용한다")
	void columnsWithNonNullValue() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String columns = sut.columns(TestEntityWithNonNullValue.class);

		// then
		assertThat(columns).contains(
			"COALESCE(ts.age, 0) AS age, "
				+ "ts.tester_id AS tester_id, "
				+ "ts.tester_nm AS tester_nm"
		);
	}

	@Test
	@DisplayName("@Column 이 붙어있지 않으면, namingStrategy 를 따른다.")
	void namingStrategyForNonColumnAnnotatedField() {
		// given
		RelationalMappingContext context = new RelationalMappingContext(new PrefixingNamingStrategy());
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String columns = sut.columns(TestOuterEntity.class);

		// then
		assertThat(columns).contains(
			"ts.x_tester_id AS x_tester_id, "
				+ "address.tester_outer_id AS address_tester_outer_id, "
				+ "ts.tester_nm AS tester_nm, "
				+ "address.cty AS address_cty, "
				+ "address.x_state AS address_x_state, "
				+ "ts.adr_cty AS adr_cty, "
				+ "ts.adr_x_state AS adr_x_state");
	}

	@Test
	@DisplayName("column 생성의 @SqlTableAlias 와 Quoting 확인")
	void columnsQuotingWithTableAlias() {
		// given
		RelationalMappingContext context = new RelationalMappingContext(new PrefixingNamingStrategy());
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(
			context,
			converter,
			new JdbcMySqlDialect(
				IdentifierProcessing.create(
					new IdentifierProcessing.Quoting("`"),
					IdentifierProcessing.LetterCasing.LOWER_CASE
				)
			)
		);

		// when
		String columns = sut.columns(TestOuterEntity.class);

		// then
		assertThat(columns).contains(
			"`ts`.`x_tester_id` AS `x_tester_id`, "
				+ "`address`.`tester_outer_id` AS `address_tester_outer_id`, "
				+ "`ts`.`tester_nm` AS `tester_nm`, "
				+ "`address`.`cty` AS `address_cty`, "
				+ "`address`.`x_state` AS `address_x_state`, "
				+ "`ts`.`adr_cty` AS `adr_cty`, "
				+ "`ts`.`adr_x_state` AS `adr_x_state`");
	}

	@Test
	@DisplayName("Root 에 @SqlTableAlias 이 있으면, column 에 Alias 가 포함된다.")
	void columnsWithRootTableAlias() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String columns = sut.columns(TestWithEntityTableAlias.class);

		// then
		assertThat(columns).contains(
			"tweta.root_id AS root_id, "
				+ "testOuter.test_root_id AS testOuter_test_root_id, "
				+ "testOuter.tester_id AS testOuter_tester_id, "
				+ "testOuter_address.tester_outer_id AS testOuter_address_tester_outer_id, "
				+ "testOuter.tester_nm AS testOuter_tester_nm, "
				+ "testOuter_address.cty AS testOuter_address_cty, "
				+ "testOuter_address.state AS testOuter_address_state, "
				+ "testOuter.adr_cty AS testOuter_adr_cty, "
				+ "testOuter.adr_state AS testOuter_adr_state"
		);
	}

	@Test
	@DisplayName("Root 에 @SqlTableAlias 이 있으면, table 에 Alias 가 포함된다.")
	void tablesWithRootTableAlias() {
		// given
		RelationalMappingContext context = new RelationalMappingContext();
		JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
			throw new UnsupportedOperationException();
		});
		SqlProvider sut = new SqlProvider(context, converter, NonQuotingDialect.INSTANCE);

		// when
		String tables = sut.tables(TestWithEntityTableAlias.class);

		// then
		assertThat(tables).contains(
			"test_table tweta "
				+ "LEFT OUTER JOIN test_outer_entity testOuter "
				+ "ON testOuter.test_root_id = tweta.root_id "
				+ "LEFT OUTER JOIN test_inner_entity testOuter_address "
				+ "ON testOuter_address.tester_outer_id = tweta.root_id"
		);
	}

	@SqlTableAlias("ts")
	static class TestOuterEntity {
		private Long testerId;

		@Column("tester_nm")
		private String testerName;

		@SqlTableAlias("address")
		@Column("tester_outer_id")
		private TestInnerEntity testInner;

		@Embedded.Nullable(prefix = "adr_")
		private TestInnerEntity testInnerEmbedded;
	}

	static class TestInnerEntity {
		@Column
		private String state;

		@Column("cty")
		private String city;
	}

	@SqlTableAlias("ts")
	static class TestEntityWithNonNullValue {
		@Column
		private Long testerId;

		@Column("tester_nm")
		private String testerName;

		@SqlFunction(expressions = {SqlFunction.COLUMN_NAME, "0"})
		@Column
		private int age;
	}

	static class TestEntityNoRootTableAlias {
		private Long rootId;

		@Column("root_name")
		private String rootName;

		@SqlTableAlias("tOuter")
		@Column("test_root_id")
		private TestOuterEntity testOuter;

		@Embedded.Nullable(prefix = "outer_")
		private TestOuterEntity testOuterEmbedded;
	}

	@Table("teie")
	static class TestEmbeddedNested {
		@Id
		private Long rootId;

		@Column("root_name")
		private String rootName;

		@Column("test_root_id")
		private TestOuterEntity testOuter;

		@Embedded.Nullable(prefix = "outer_")
		private TestOuterEntity testOuterEmbedded;

		@Embedded.Nullable
		private TestEntityWithNonNullValue function;
	}

	@Table("test_table")
	@SqlTableAlias("tweta")
	static class TestWithEntityTableAlias {
		@Id
		private Long rootId;

		@Column("test_root_id")
		private TestOuterEntity testOuter;
	}

	private static class PrefixingNamingStrategy implements NamingStrategy {

		@Override
		public String getColumnName(RelationalPersistentProperty property) {
			return "x_" + NamingStrategy.super.getColumnName(property);
		}

	}
}
