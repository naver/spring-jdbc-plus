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

package com.navercorp.spring.data.jdbc.plus.sql.guide.board

import com.navercorp.spring.data.jdbc.plus.sql.guide.board.sql.BoardSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport
import org.springframework.data.repository.CrudRepository

interface BoardRepository : CrudRepository<Board, Long>, BoardRepositoryCustom

/**
 * @author Myeonghyeon Lee
 */
interface BoardRepositoryCustom {
    fun findGraphById(id: Long): Board?

    fun findAllGraph(): List<Board>

    fun findPostDtoByPostId(postId: Long): PostDto?
}

class BoardRepositoryImpl(entityProvider: EntityJdbcProvider) : JdbcRepositorySupport<Board>(
    Board::class.java,
    entityProvider
), BoardRepositoryCustom {
    private val sqls: BoardSql = super.sqls(::BoardSql)

    override fun findGraphById(id: Long): Board? {
        val sql = this.sqls.selectGraphById()
        return findOne(
            sql,
            mapParameterSource()
                .addValue("boardId", id)
        ).orElse(null)
    }

    override fun findAllGraph(): List<Board> {
        val sql = this.sqls.selectAllGraph()
        return find(
            sql,
            mapParameterSource()
        )
    }

    override fun findPostDtoByPostId(postId: Long): PostDto? {
        val sql = this.sqls.selectPostDtoByPostId()
        return findOne(
            sql,
            mapParameterSource()
                .addValue("postId", postId),
            PostDto::class.java
        ).orElse(null)
    }
}
