/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.sql.guide.board.sql

import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.PostDto
import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

/**
 * @author Myeonghyeon Lee
 */
class BoardSql : SqlGeneratorSupport() {

    fun selectGraphById(): String = """
        SELECT ${sql.aggregateColumns(Board::class.java)}

        FROM ${sql.aggregateTables(Board::class.java)} 

        WHERE n_board.id = :boardId
        ORDER BY posts.board_index, posts_comments.post_index
        """

    fun selectAllGraph(): String = """
        SELECT ${sql.aggregateColumns(Board::class.java)}

        FROM ${sql.aggregateTables(Board::class.java)}
        
        ORDER BY n_board.id, posts.board_index, posts_comments.post_index
        """

    fun selectPostDtoByPostId(): String = """
        SELECT ${sql.aggregateColumns(PostDto::class.java)}
        
        FROM n_post AS post
        LEFT OUTER JOIN n_audit AS post_audit
        ON post_audit.post_id = post.id
        LEFT OUTER JOIN n_audit_secret AS post_audit_secret
        ON post_audit_secret.audit_id = post_audit.id
        
        LEFT OUTER JOIN n_tag AS post_tags
        ON post_tags.post_id = post.id
        
        LEFT OUTER JOIN n_comment AS post_comments
        ON post_comments.post_id = post.id
        LEFT OUTER JOIN n_audit AS post_comments_audit
        ON post_comments_audit.comment_id = post_comments.id
        LEFT OUTER JOIN n_audit_secret AS post_comments_audit_secret
        ON post_comments_audit_secret.audit_id = post_comments_audit.id
        
        LEFT OUTER JOIN n_config AS post_configMap
        ON post_configMap.post_id = post.id
        
        LEFT OUTER JOIN n_label AS labels
        ON post.board_id = labels.board_id
        
        WHERE post.id = :postId
        """
}
