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

import com.navercorp.spring.jdbc.plus.commons.annotations.SqlTableAlias
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Myeonghyeon Lee
 */
@Table("n_board")
data class Board(
    @Id
    val id: Long? = null,

    val name: String,

    @MappedCollection(idColumn = "board_id")
    val labels: Set<Label>,

    @MappedCollection(idColumn = "board_id", keyColumn = "board_index")
    val posts: List<Post>,

    @SqlTableAlias("b_audit")
    @Column("board_id")
    val audit: Audit?,

    @Embedded.Nullable(prefix = "board_")
    val memo: Memo?,

    @MappedCollection(idColumn = "board_id", keyColumn = "config_key")
    val configMap: Map<String, Config> = HashMap()
)

@Table("n_label")
data class Label(
    @Id
    val id: Long? = null,

    val name: String
)

@Table("n_post")
data class Post(
    @Id
    val id: Long? = null,

    val postNo: Long,

    val title: String,

    val content: String,

    @MappedCollection(idColumn = "post_id")
    val tags: Set<Tag> = HashSet(),

    @MappedCollection(idColumn = "post_id", keyColumn = "post_index")
    val comments: List<Comment> = ArrayList(),

    @Column("post_id")
    val audit: Audit?,

    @Embedded.Nullable
    val memo: Memo?,

    @MappedCollection(idColumn = "post_id", keyColumn = "config_key")
    val configMap: Map<String, Config> = HashMap()
)

@Table("n_tag")
data class Tag(
    @Id
    var id: Long? = null,

    var content: String
)

@Table("n_comment")
data class Comment(
    @Id
    val id: Long? = null,

    val content: String,

    @Column("comment_id")
    val audit: Audit?
)

@Table("n_audit")
data class Audit(
    @Id
    val id: Long? = null,

    val name: String,

    @Embedded.Nullable
    val memo: Memo?,

    @Column("audit_id")
    val secret: AuditSecret?
)

data class Memo(
    val memo: String? = null
)

@Table("n_audit_secret")
data class AuditSecret(
    @Id
    val id: Long? = null,

    val secret: String
)

@Table("n_config")
data class Config(
    @Id
    val id: Long? = null,

    val configKey: String,

    val configValue: String
)
