package com.navercorp.spring.data.jdbc.plus.sql.guide.board

import com.navercorp.spring.data.jdbc.plus.sql.guide.board.sql.BoardSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport
import org.springframework.data.repository.CrudRepository

interface BoardRepository : CrudRepository<Board, Long>, BoardRepositoryCustom

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
            sql, mapParameterSource()
                .addValue("boardId", id)
        ).orElse(null)
    }

    override fun findAllGraph(): List<Board> {
        val sql = this.sqls.selectAllGraph()
        return find(
            sql, mapParameterSource()
        )
    }

    override fun findPostDtoByPostId(postId: Long): PostDto? {
        val sql = this.sqls.selectPostDtoByPostId()
        return findOne(
            sql, mapParameterSource()
                .addValue("postId", postId),
            PostDto::class.java
        ).orElse(null)
    }
}
