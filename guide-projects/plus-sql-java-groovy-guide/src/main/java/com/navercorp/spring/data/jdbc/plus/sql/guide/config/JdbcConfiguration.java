package com.navercorp.spring.data.jdbc.plus.sql.guide.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

import com.navercorp.spring.data.jdbc.plus.sql.parametersource.EntityConvertibleSqlParameterSourceFactory;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory;
import com.navercorp.spring.jdbc.plus.support.parametersource.ConvertibleParameterSourceFactory;
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter;
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.NoneFallbackParameterSource;

@Configuration
public class JdbcConfiguration {

	@SuppressWarnings("unchecked")
	@Bean
	@Primary
	public SqlParameterSourceFactory sqlParameterSourceFactory(
		RelationalMappingContext mappingContext,
		JdbcConverter jdbcConverter,
		Dialect dialect) {
		List<?> dialectConverters = (List<?>) dialect.getConverters();
		List<Converter<?, ?>> converters = storeConverters();
		converters.addAll((List<Converter<?, ?>>) dialectConverters);

		return new EntityConvertibleSqlParameterSourceFactory(
			new ConvertibleParameterSourceFactory(
				new DefaultJdbcParameterSourceConverter(converters),
				new NoneFallbackParameterSource()
			),
			mappingContext,
			jdbcConverter,
			IdentifierProcessing.ANSI
		);
	}

	private static List<Converter<?, ?>> storeConverters() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		for (Object obj : JdbcCustomConversions.storeConverters()) {
			if (obj instanceof Converter<?, ?>
				&& obj.getClass().getAnnotation(ReadingConverter.class) == null
			) {
				converters.add((Converter<?, ?>) obj);
			}
		}
		return converters;
	}
}
