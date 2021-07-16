package com.navercorp.spring.data.jdbc.plus.sql.guide.config

import com.navercorp.spring.data.jdbc.plus.sql.parametersource.EntityConvertibleSqlParameterSourceFactory
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.SqlParameterSourceFactory
import com.navercorp.spring.jdbc.plus.support.parametersource.ConvertibleParameterSourceFactory
import com.navercorp.spring.jdbc.plus.support.parametersource.converter.DefaultJdbcParameterSourceConverter
import com.navercorp.spring.jdbc.plus.support.parametersource.fallback.NoneFallbackParameterSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.convert.JdbcConverter
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.IdentifierProcessing

@Configuration
class JdbcConfiguration {
    @Bean
    fun sqlParameterSourceFacotry(
        mappingContext: RelationalMappingContext,
        jdbcConverter: JdbcConverter
    ): SqlParameterSourceFactory {
        return EntityConvertibleSqlParameterSourceFactory(
            ConvertibleParameterSourceFactory(DefaultJdbcParameterSourceConverter(), NoneFallbackParameterSource()),
            mappingContext,
            jdbcConverter,
            IdentifierProcessing.ANSI
        )
    }
}
