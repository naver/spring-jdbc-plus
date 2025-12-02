package com.navercorp.spring.data.jdbc.plus.repository.guide.config;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import com.navercorp.spring.data.jdbc.plus.repository.config.AbstractJdbcPlusConfiguration;
import com.navercorp.spring.data.jdbc.plus.repository.guide.article.EnumStateArticle;
import com.navercorp.spring.data.jdbc.plus.repository.guide.article.EnumStateArticle.State;

@Configuration
public class JdbcConfiguration extends AbstractJdbcPlusConfiguration {

	@Override
	protected List<?> userConverters() {
		return List.of(
			new ArticleStateReadingConverter(),
			new ArticleStateWritingConverter()
		);
	}

	@WritingConverter
	private static class ArticleStateWritingConverter implements Converter<EnumStateArticle.State, String> {
		@Override
		public String convert(EnumStateArticle.State source) {
			return source.getCode();
		}
	}

	@ReadingConverter
	private static class ArticleStateReadingConverter implements Converter<String, EnumStateArticle.State> {
		@Override
		public @Nullable State convert(String source) {
			return Arrays.stream(EnumStateArticle.State.values())
				.filter(it -> it.getCode().equals(source))
				.findFirst()
				.orElse(null);
		}
	}
}
