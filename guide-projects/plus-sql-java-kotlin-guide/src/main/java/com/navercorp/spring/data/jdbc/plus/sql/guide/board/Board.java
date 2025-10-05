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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.With;

import com.navercorp.spring.jdbc.plus.commons.annotations.SqlTableAlias;

/**
 * @author Myeonghyeon Lee
 */
@Table("n_board")
@Getter
@Builder
public class Board {
	@Id
	private Long id;

	private String name;

	@MappedCollection(idColumn = "board_id")
	@Builder.Default
	private Set<Label> labels = new HashSet<>();

	@MappedCollection(idColumn = "board_id", keyColumn = "board_index")
	@Builder.Default
	private List<Post> posts = new ArrayList<>();

	@SqlTableAlias("b_audit")
	@Column("board_id")
	private Audit audit;

	@Embedded.Nullable(prefix = "board_")
	private Memo memo;

	@MappedCollection(idColumn = "board_id", keyColumn = "config_key")
	@Builder.Default
	private Map<String, Config> configMap = new HashMap<>();

	@Table("n_label")
	@Getter
	@Builder
	public static class Label {
		@Id
		private Long id;

		private String name;
	}

	@Table("n_post")
	@Getter
	@Builder
	public static class Post {
		@Id
		private Long id;

		private Long postNo;

		private String title;

		private String content;

		@MappedCollection(idColumn = "post_id")
		@Builder.Default
		private Set<Tag> tags = new HashSet<>();

		@MappedCollection(idColumn = "post_id", keyColumn = "post_index")
		@Builder.Default
		private List<Comment> comments = new ArrayList<>();

		@Column("post_id")
		private Audit audit;

		@Embedded.Nullable
		private Memo memo;

		@MappedCollection(idColumn = "post_id", keyColumn = "config_key")
		@Builder.Default
		private Map<String, Config> configMap = new HashMap<>();
	}

	@Table("n_tag")
	@Value
	@Builder
	public static class Tag {
		@Id
		@With
		Long id;

		String content;
	}

	@Table("n_comment")
	@Getter
	@Builder
	public static class Comment {
		@Id
		private Long id;

		private String content;

		@Column("comment_id")
		private Audit audit;
	}

	@Table("n_audit")
	@Getter
	@Builder
	public static class Audit {
		@Id
		private Long id;

		private String name;

		@Embedded.Nullable
		private Memo memo;

		@Column("audit_id")
		private AuditSecret secret;
	}

	@Value
	@Builder
	public static class Memo {
		String memo;
	}

	@Table("n_audit_secret")
	@Value
	@Builder
	public static class AuditSecret {
		@Id
		@With
		Long id;

		String secret;
	}

	@Table("n_config")
	@Value
	@Builder
	public static class Config {
		@Id
		@With
		Long id;

		String configKey;

		String configValue;
	}
}
