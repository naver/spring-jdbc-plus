[![GitHub release](https://img.shields.io/github/v/release/naver/spring-jdbc-plus.svg)](https://img.shields.io/github/v/release/naver/spring-jdbc-plus.svg?include_prereleases)
[![GitHub license](https://img.shields.io/github/license/naver/spring-jdbc-plus.svg)](https://github.com/naver/spring-jdbc-plus.js/blob/master/LICENSE)

# Spring JDBC Plus ![build](https://github.com/naver/spring-jdbc-plus/actions/workflows/gradle.yml/badge.svg) [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/naver/spring-jdbc-plus) [![Project Diagrams](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/naverspringjdbcplus/)

**⚠️ Please do not use 3.3.3** <br>
Spring JDBC Plus provides [Spring Data JDBC](https://github.com/spring-projects/spring-data-relational) based extension.
It provides necessary features when writing more complex SQL than the functions supported by `CrudRepository`.
If you need to use Spring Data JDBC's Persistence features and SQL execution function in combination, `Spring JDBC Plus`
may be an appropriate choice.

## Features

- Support for executing custom `SQL SELECT` statements
- Provide `BeanParameterSource`, `MapParameterSource`, `EntityParameterSource`
- Provide parameter source converters such as `Java8Time`,`Enum`, etc.
- Entity mapping support for complex table join SELECT results
- `AggregateResultSet` supports mapping of `1: N` result data to `Aggregate` object graph by `LEFT OUTER JOIN` lookup
- `JdbcRepository` provides insert / update syntax
- Support for setting `Reactive (Flux / Mono)` type as the return type of `CustomRepository` method

- [User Guide](https://github.com/naver/spring-jdbc-plus/wiki)

## Getting Started (Spring Boot Starter Data JDBC Plus SQL)

* Gradle

    ```gradle
    buildscript {
        repositories {
            mavenCentral()
            mavenLocal()
            maven {
                url "https://repo.spring.io/milestone/"
            }
        }
        dependencies {
            classpath("org.springframework.boot:spring-boot-gradle-plugin:3.4.6")
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
        implementation("com.navercorp.spring:spring-boot-starter-data-jdbc-plus-sql:3.4.6")
    }
    ```

* Maven

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.6</version>
        <relativePath/>
    </parent>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>

    <dependency>
        <groupId>com.navercorp.spring</groupId>
        <artifactId>spring-boot-starter-data-jdbc-plus-sql</artifactId>
        <version>3.4.6</version>
    </dependency>
    ```

* Java Codes

    ```java
    @Table("n_order")
    @Data
    public class Order {
        @Id
        @Column("order_no")
        private Long orderNo;

        @Column("price")
        private long price;

        @Column("purchaser_no")
        private String purchaserNo;
    }

    public interface OrderRepository extends CrudRepository<Order, Long>, OrderRepositoryCustom {
    }

    public interface OrderRepositoryCustom {
        List<Order> findByPurchaserNo(String purchaserNo);
    }

    public class OrderRepositoryImpl extends JdbcRepositorySupport<Order> implements OrderRepositoryCustom {
        private final OrderSql sqls;

        public OrderRepositoryImpl(EntityJdbcProvider entityJdbcProvider) {
            super(Order.class, entityJdbcProvider);
            this.sql = sqls(OrderSql::new);
        }

        @Override
        public List<Order> findByPurchaserNo(String purchaserNo) {
            String sql = this.sql.selectByPurchaserNo();
            return find(sql, mapParameterSource()
                .addValue("purchaserNo", purchaserNo));
        }
    }
    ```

* Groovy codes for SQL

    ```groovy
    class OrderSql extends SqlGeneratorSupport {

        String selectByPurchaserNo() {
            """
            SELECT ${sql.columns(Order)}
            FROM n_order
            WHERE purchaser_no = :purchaserNo
            """
        }
    }
    ```

### Cautions when writing SQL

- Must use named parameters to pass parameters to SQL.
- If parameter values are concatenated directly to String, it produces bad effects.
    - May cause SQL injection vulnerability.
    - Reduce efficiency of caches in PreparedStatement and NamedParameterJdbcTemplate

Be careful when use string interpolation in Groovy and Kotlin.

* Bad  :-1:
    ```groovy
    class OrderSql extends SqlGeneratorSupport {

        String selectByPurchaserNo(String purchaserNo) {
        """
        SELECT ${sql.columns(Order)}
        FROM n_order
        WHERE purchaser_no = '${purchaserNo}'
        """
        }
    }
    ```

* Good :+1:
    ```groovy
    class OrderSql extends SqlGeneratorSupport {

        String selectByPurchaserNo() {
        """
        SELECT ${sql.columns(Order)}
        FROM n_order
        WHERE purchaser_no = :purchaserNo
        """
        }
    }
    ```

## Annotation Guide

### @SqlTableAlias

``` JAVA
@Value
@Builder
@Table("post")
public class PostDto {
    @Id
    Long id;

    @Column
    Post post;

    @SqlTableAlias("p_labels")
    @MappedCollection(idColumn = "board_id")
    Set<Label> labels;
}
```

`@SqlTableAlias` is used to attach a separate identifier to the table. `@SqlTableAlias` can be applied to class, field
and method.

`@SqlTableAlias` is used in the form of `@SqlTableAlias("value")` .

### @SqlFunction

```java

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
```

`@SqlFunction` is typically used to map fields or methods of entity classes to SQL functions.

For example, it can be utilized to define default values for certain fields, or to transform values based on specific
conditions.

### @SoftDeleteColumn

```java

@Value
@Builder
@Table("article")
static class SoftDeleteArticle {
	@Id
	Long id;

	String contents;

	@SoftDeleteColumn.Boolean(valueAsDeleted = "true")
	boolean deleted;
}
```

`@SoftDeleteColumn` supports the soft delete, which is considered as deleted but does not delete actually.
This replaces the default 'DELETE' operations to 'UPDATE' operations, by updating specific columns.

You can use value types `Boolean` or `String` by Declaring `@SoftDeleteColumn.Boolean` or `@SoftDeleteColumn.String`.

## Examples

* [Java + Groovy SQL Example](./guide-projects/plus-sql-java-groovy-guide)
* [Java + Kotlin SQL Example](./guide-projects/plus-sql-java-kotlin-guide)
* [Kotlin Example](./guide-projects/plus-sql-kotlin-guide)

## Getting Help

- [User Guide](https://github.com/naver/spring-jdbc-plus/wiki)
- [Reporting Issues](https://github.com/naver/spring-jdbc-plus/issues)

## Coding Convention

- [naver hackday-conventions-java](https://naver.github.io/hackday-conventions-java/)
- [naver/hackday-conventions-java](https://github.com/naver/hackday-conventions-java)
- checkstyle: ./rule/naver-checkstyle-rules.xml
- intellij-formatter:
  ./rule/naver-intellij-formatter.xml (https://naver.github.io/hackday-conventions-java/#editor-config)

## Building from Source

```
$  ./gradlew clean build
```

## License

```
   Copyright 2020-2021 NAVER Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
