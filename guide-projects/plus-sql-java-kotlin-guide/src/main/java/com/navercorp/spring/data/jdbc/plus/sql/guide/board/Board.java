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

package com.navercorp.spring.data.jdbc.plus.sql.guide.board;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

import com.navercorp.spring.jdbc.plus.commons.annotations.SqlTableAlias;

/**
 * @author Myeonghyeon Lee
 */
@Builder(toBuilder = true)
@Table("n_board")
public record Board(
	@Id
	Long id,

	String name,

	@MappedCollection(idColumn = "board_id")
	@Nullable Set<Label> labels,

	@MappedCollection(idColumn = "board_id", keyColumn = "board_index")
	@Nullable List<Post> posts,

	@SqlTableAlias("b_audit")
	@Column("board_id")
	@Nullable Audit audit,

	@Embedded.Nullable(prefix = "board_")
	@Nullable Memo memo,

	@MappedCollection(idColumn = "board_id", keyColumn = "config_key")
	@Nullable Map<String, Config> configMap
) {
	public Board {
		labels = labels == null ? Set.of() : labels;
		posts = posts == null ? List.of() : posts;
		configMap = configMap == null ? Map.of() : configMap;
	}

	public Board sort() {
		return toBuilder()
			.labels(labels.stream()
				.sorted(comparing(label -> label.id().title()))
				.collect(toCollection(LinkedHashSet::new)))
			.posts(posts.stream()
				.map(Post::sort)
				.sorted(comparing(Post::id))
				.toList())
			.audit(audit != null ? audit.sort() : null)
			.build();
	}

	@Builder(toBuilder = true)
	@Table("n_label")
	public record Label(
		@Id
		@Nullable
		@Embedded.Empty
		LabelId id,

		String name
	) implements Persistable<LabelId> {
		@Override
		public @Nullable LabelId getId() {
			return id;
		}

		@Override
		public boolean isNew() {
			return false;
		}
	}

	@Builder(toBuilder = true)
	public record LabelId(
		@Column("title")
		String title,

		@Column("project_name")
		String projectName
	) {
	}

	@Builder(toBuilder = true)
	@Table("n_post")
	public record Post(
		@Id
		Long id,

		Long postNo,

		String title,

		String content,

		@MappedCollection(idColumn = "post_id")
		@Nullable Set<Tag> tags,

		@MappedCollection(idColumn = "post_id", keyColumn = "post_index")
		@Nullable List<Comment> comments,

		@Column("post_id")
		@Nullable Audit audit,

		@Embedded.Nullable
		@Nullable Memo memo,

		@MappedCollection(idColumn = "post_id", keyColumn = "config_key")
		@Nullable Map<String, Config> configMap
	) {
		public Post {
			tags = tags == null ? Set.of() : tags;
			comments = comments == null ? List.of() : comments;
			configMap = configMap == null ? Map.of() : configMap;
		}

		public Post sort() {
			return toBuilder()
				.tags(tags.stream()
					.sorted(comparing(Tag::id))
					.collect(toCollection(LinkedHashSet::new)))
				.comments(comments.stream()
					.map(Comment::sort)
					.sorted(comparing(Comment::id))
					.toList())
				.audit(audit != null ? audit.sort() : null)
				.build();
		}
	}

	@Builder(toBuilder = true)
	@Table("n_tag")
	public record Tag(
		@Id
		Long id,

		String content
	) {
	}

	@Builder(toBuilder = true)
	@Table("n_comment")
	public record Comment(
		@Id
		Long id,

		String content,

		@Column("comment_id")
		@Nullable Audit audit
	) {
		public Comment sort() {
			return toBuilder()
				.audit(audit != null ? audit.sort() : null)
				.build();
		}
	}

	@Builder(toBuilder = true)
	@Table("n_audit")
	public record Audit(
		@Id
		Long id,

		String name,

		@Embedded.Nullable
		@Nullable Memo memo,

		@Column("audit_id")
		@Nullable AuditSecret secret
	) {
		public Audit sort() {
			return this;
		}
	}

	@Builder(toBuilder = true)
	public record Memo(
		String memo
	) {
	}

	@Builder(toBuilder = true)
	@Table("n_audit_secret")
	public record AuditSecret(
		@Id
		Long id,

		String secret
	) {
	}

	@Builder(toBuilder = true)
	@Table("n_config")
	public record Config(
		@Id
		Long id,

		String configKey,

		String configValue
	) {
	}
}
