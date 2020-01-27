package com.navercorp.spring.data.jdbc.plus.sql.convert;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.mapping.Table;

class SqlProviderTest {
	@Test
	@DisplayName("테이블 기본 컬럼 생성 규칙")
	@Disabled
	void columns() {
		// given
		SqlProvider sut = null; //new SqlProvider(new RelationalMappingContext());

		// when
		String columns = sut.columns(TestInnerEntity.class);

		// then
		assertThat(columns).isEqualTo(
			"test_inner_entity.cty AS cty,\n"
				+ "test_inner_entity.state AS state");
	}

	@Test
	@DisplayName("Embedded 된 column을 찾아가서 변환한다.")
	@Disabled("SqlTableAlias 를 지원할 때까지 테스트를 Disabled 한다.")
	void columnsWithEmbedded() {
		// given
		SqlProvider sut = null; // new SqlProvider(new RelationalMappingContext());

		// when
		String columns = sut.columns(TestOuterEntity.class);

		// then
		assertThat(columns).isEqualTo(
			"ts.tester_id AS tester_id,\n"
				+ "ts.tester_nm AS tester_nm,\n"
				+ "address.state AS adr_state,\n"
				+ "address.cty AS adr_cty");
	}

	@Test
	@DisplayName("@SqlTemplate 은 Column 변환식을 사용한다")
	@Disabled("SqlTableAlias 를 지원할 때까지 테스트를 Disabled 한다.")
	void columnsWithNonNullValue() {
		// given
		SqlProvider sut = null; // new SqlProvider(new RelationalMappingContext());

		// when
		String columns = sut.columns(TestEntityWithNonNullValue.class);

		// then
		assertThat(columns).isEqualTo(
			"ts.tester_id AS tester_id,\n"
				+ "ts.tester_nm AS tester_nm,\n"
				+ "COALESCE(ts.age, 0) as age");
	}

	@Test
	@DisplayName("@Column 이 붙어있지 않으면, namingStrategy 를 따른다.")
	@Disabled("SqlTableAlias 를 지원할 때까지 테스트를 Disabled 한다.")
	void namingStrategyForNonColumnAnnotatedField() {
		// given
		SqlProvider sut = null; //new SqlProvider(new RelationalMappingContext(new PrefixingNamingStrategy()));

		// when
		String columns = sut.columns(TestOuterEntity.class);

		// then
		assertThat(columns).isEqualTo(
			"ts.x_tester_id AS x_tester_id,\n"
				+ "ts.tester_nm AS tester_nm,\n"
				+ "address.x_state AS adr_x_state,\n"
				+ "address.cty AS adr_cty");
	}

	// @SqlTableAlias("ts")
	static class TestOuterEntity {
		private Long testerId;

		@Column("tester_nm")
		private String testerName;

		// @SqlTableAlias("address")
		@Embedded.Nullable(prefix = "adr_")
		private TestInnerEntity testInner;
	}

	static class TestInnerEntity {
		@Column
		private String state;

		@Column("cty")
		private String city;
	}

	// @SqlTableAlias("ts")
	static class TestEntityWithNonNullValue {
		@Column
		private Long testerId;

		@Column("tester_nm")
		private String testerName;

		@Column
		private int age;
	}

	// @SqlTableAlias("ts")
	static class TestEntityWithIgnoredColumn {
		private Long testerId;

		@Column("tester_nm")
		private String testerName;

		private String phoneNumber; // This column will be ignored
	}

	@Table("teie")
	static class TestEmbeddedIgnoreEntity {
		private Long testerId;

		@Column("tester_nm")
		private String testerName;

		@Embedded.Nullable(prefix = "adr_")
		private TestInnerEntity testInner;
	}

	private static class PrefixingNamingStrategy implements NamingStrategy {

		@Override
		public String getColumnName(RelationalPersistentProperty property) {
			return "x_" + NamingStrategy.super.getColumnName(property);
		}

	}
}
