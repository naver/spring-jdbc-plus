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
class BoardSql extends SqlGeneratorSupport {
    // ${sql.aggregateColumns(Board)}
    //        n_board.id AS id, n_board.name AS name, n_board.board_memo AS board_memo,
    //        audit.id AS audit_id, audit.name AS audit_name, audit.memo AS audit_memo, audit_secret.id AS audit_secret_id, audit_secret.secret AS audit_secret_secret,
    //        labels.id AS labels_id, labels.name AS labels_name,
    //        configMap.id AS configmap_id, configMap.config_key AS configmap_config_key, configMap.config_value AS configmap_config_value,
    //        posts.id AS posts_id, posts.post_no AS posts_post_no, posts.title AS posts_title, posts.content AS posts_content, posts.memo AS posts_memo,
    //        posts_audit.id AS posts_audit_id, posts_audit.name AS posts_audit_name, posts_audit.memo AS posts_audit_memo, posts_audit_secret.id AS posts_audit_secret_id, posts_audit_secret.secret AS posts_audit_secret_secret,
    //        posts_tags.id AS posts_tags_id, posts_tags.content AS posts_tags_content,
    //        posts_comments.id AS posts_comments_id, posts_comments.content AS posts_comments_content,
    //        posts_comments_audit.id AS posts_comments_audit_id, posts_comments_audit.name AS posts_comments_audit_name, posts_comments_audit.memo AS posts_comments_audit_memo,
    //        posts_comments_audit_secret.id AS posts_comments_audit_secret_id, posts_comments_audit_secret.secret AS posts_comments_audit_secret_secret,
    //        posts_configMap.id AS posts_configmap_id, posts_configmap.config_key AS posts_configmap_config_key, posts_configMap.config_value AS posts_configmap_config_value

    // ${sql.aggregateTables(Board)}
    //        n_board
    //        LEFT OUTER JOIN n_audit AS audit
    //        ON audit.board_id = n_board.id
    //        LEFT OUTER JOIN n_audit_secret AS audit_secret
    //        ON audit_secret.audit_id = audit.id
    //
    //        LEFT OUTER JOIN n_label AS labels
    //        ON labels.board_id = n_board.id
    //
    //        LEFT OUTER JOIN n_config AS configMap
    //        ON configMap.board_id = n_board.id
    //
    //        LEFT OUTER JOIN n_post AS posts
    //        ON posts.board_id = n_board.id
    //        LEFT OUTER JOIN n_audit AS posts_audit
    //        ON posts_audit.post_id = posts.id
    //        LEFT OUTER JOIN n_audit_secret AS posts_audit_secret
    //        ON posts_audit_secret.audit_id = posts_audit.id
    //
    //        LEFT OUTER JOIN n_tag AS posts_tags
    //        ON posts_tags.post_id = posts.id
    //
    //        LEFT OUTER JOIN n_comment AS posts_comments
    //        ON posts_comments.post_id = posts.id
    //        LEFT OUTER JOIN n_audit AS posts_comments_audit
    //        ON posts_comments_audit.comment_id = posts_comments.id
    //        LEFT OUTER JOIN n_audit_secret AS posts_comments_audit_secret
    //        ON posts_comments_audit_secret.audit_id = posts_comments_audit.id
    //
    //        LEFT OUTER JOIN n_config AS posts_configMap
    //        ON posts_configMap.post_id = posts.id
    //

    String selectGraphById() {
        """
        SELECT ${sql.aggregateColumns(Board)}

        FROM ${sql.aggregateTables(Board)} 

        WHERE n_board.id = :boardId
        ORDER BY posts.board_index, posts_comments.post_index
        """
    }

    String selectAllGraph() {
        """
        SELECT ${sql.aggregateColumns(Board)}

        FROM ${sql.aggregateTables(Board)}
        
        ORDER BY n_board.id, posts.board_index, posts_comments.post_index
        """
    }

    String selectPostDtoByPostId() {
        """
        SELECT ${sql.aggregateColumns(PostDto)}
        
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
        
        LEFT OUTER JOIN n_label AS p_labels
        ON post.board_id = p_labels.board_id
        
        WHERE post.id = :postId
        """
    }
}
