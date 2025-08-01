package com.navercorp.spring.data.jdbc.plus.support.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.Table;

import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn;

class SoftDeletePropertyTest {

	RelationalMappingContext context = JdbcMappingContext.forQuotedIdentifiers();

	@Test
	void createBooleanValueSoftDeleteProperty() {
		// given
		RelationalPersistentEntity<?> entity = createRelationalPersistentEntity(BooleanValueSoftDeleteArticle.class);

		// when
		SoftDeleteProperty actual = SoftDeleteProperty.from(entity);

		// then
		assertThat(actual.exists()).isTrue();
		assertThat(actual.getColumnName().getReference()).isEqualTo("deleted");
		assertThat(actual.getUpdateValue()).isEqualTo(true);
	}

	@Test
	void createEmptySoftDeleteProperty() {
		// given
		RelationalPersistentEntity<?> entity = createRelationalPersistentEntity(Article.class);

		// when
		SoftDeleteProperty actual = SoftDeleteProperty.from(entity);

		// then
		assertThat(actual.exists()).isFalse();
	}

	@ParameterizedTest
	@MethodSource("retrieveValueFunctionSource")
	void throwsWhenRetrieveValueOfEmptySoftDeleteProperty(
		Function<SoftDeleteProperty, Object> retrieveValueFunction
	) {
		// given
		RelationalPersistentEntity<?> entity = createRelationalPersistentEntity(Article.class);
		SoftDeleteProperty softDeleteProperty = SoftDeleteProperty.from(entity);

		// when-then
		assertThatThrownBy(() -> retrieveValueFunction.apply(softDeleteProperty))
			.isExactlyInstanceOf(IllegalStateException.class)
			.hasMessage("SoftDeleteProperty should exist to use columnName/updateValue.");
	}

	@Test
	void throwsWhenInvalidBooleanUpdateValue() {
		// given
		RelationalPersistentEntity<?> entity =
			createRelationalPersistentEntity(InvalidBooleanValueSoftDeleteArticle.class);

		// when-then
		assertThatThrownBy(() -> SoftDeleteProperty.from(entity))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContainingAll("Invalid value", "provided for Boolean type of SoftDeleteColumn");
	}

	private static Stream<Function<SoftDeleteProperty, Object>> retrieveValueFunctionSource() {
		return Stream.of(
			SoftDeleteProperty::getColumnName,
			SoftDeleteProperty::getUpdateValue
		);
	}

	private RelationalPersistentEntity<?> createRelationalPersistentEntity(Class<?> classType) {
		return context.getRequiredPersistentEntity(classType);
	}

	@Table("article")
	static class Article {

		@Id
		Long id;

		String contents;
	}

	@Table("boolean_value_article")
	static class BooleanValueSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn.Boolean(valueAsDeleted = "true")
		boolean deleted;
	}

	@Table("invalid_boolean_value_article")
	static class InvalidBooleanValueSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn.Boolean(valueAsDeleted = "tx")
		boolean deleted;
	}

	@Table("string_value_article")
	static class StringValueSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn.String(valueAsDeleted = "DELETE")
		ArticleState state;

		enum ArticleState {
			OPEN, CLOSE, DELETE
		}
	}
}
