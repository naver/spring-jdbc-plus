package com.navercorp.spring.data.jdbc.plus.sql.guide.board

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table

@Table("post")
data class PostDto(
    @Id
    val id: Long,

    @Column
    val post: Post,

    @MappedCollection(idColumn = "board_id")
    val labels: Set<Label>
)
