package com.navercorp.spring.data.jdbc.plus.sql.guide

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.relational.core.dialect.Dialect
import org.springframework.data.relational.core.dialect.MySqlDialect

@SpringBootApplication
class Application {

    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }

    @Bean
    @Primary
    fun mysqlDialect(): Dialect {
        return MySqlDialect.INSTANCE
    }
}
