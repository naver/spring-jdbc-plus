package com.navercorp.spring.data.jdbc.plus.sql.guide.config

import com.navercorp.spring.data.jdbc.plus.sql.parametersource.EntityConvertibleSqlParameterSourceFactory
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory
import com.navercorp.spring.jdbc.plus.support.parametersource.ConvertibleParameterSourceFactory
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.NoneFallbackParameterSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.jdbc.core.convert.JdbcConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.core.dialect.JdbcMySqlDialect
import org.springframework.data.relational.core.dialect.Dialect
import org.springframework.data.relational.core.mapping.RelationalMappingContext

@Configuration
class JdbcConfiguration {
    @Suppress("UNCHECKED_CAST")
    @Bean
    fun sqlParameterSourceFactory(
        mappingContext: RelationalMappingContext,
        jdbcConverter: JdbcConverter,
        dialect: Dialect
    ): SqlParameterSourceFactory {
        val converters = storeConverters()
        converters.addAll(dialect.converters as List<Converter<*, *>>)

        return EntityConvertibleSqlParameterSourceFactory(
            ConvertibleParameterSourceFactory(
                DefaultJdbcParameterSourceConverter(converters),
                NoneFallbackParameterSource()
            ),
            mappingContext,
            jdbcConverter
        )
    }

    @Bean
    fun jdbcDialect(): Dialect = JdbcMySqlDialect.INSTANCE

    private fun storeConverters(): MutableList<Converter<*, *>> {
        val result = mutableListOf<Converter<*, *>>()

        JdbcCustomConversions.storeConverters().forEach {
            if (it is Converter<*, *> && it.javaClass.getAnnotation(ReadingConverter::class.java) == null) {
                result.add(it)
            }
        }
        return result
    }
}
