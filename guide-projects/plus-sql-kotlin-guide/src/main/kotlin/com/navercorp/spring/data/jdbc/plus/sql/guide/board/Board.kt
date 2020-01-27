package com.navercorp.spring.data.jdbc.plus.sql.guide.board

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.util.ArrayList

@Table("n_board")
data class Board(
    @Id
    val id: Long? = null,

    val name: String,

    @MappedCollection(idColumn = "board_id")
    val labels: Set<Label>,

    @MappedCollection(idColumn = "board_id", keyColumn = "board_index")
    val posts: List<Post>,

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
